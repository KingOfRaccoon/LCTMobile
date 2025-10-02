package ru.skittens.shared.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import ru.skittens.shared.model.DocumentMeta
import ru.skittens.shared.model.ScreenDefinition
import ru.skittens.shared.model.ScreenSchema
import ru.skittens.shared.model.ScreenSections
import ru.skittens.shared.model.SchemaNode
import ru.skittens.shared.network.RemoteDataSource

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenRepositoryTest {
    @Test
    fun `streamScreen emits idle loading success`() = runTest {
        val schema = sampleSchema("main")
        val repoScope = TestScope(StandardTestDispatcher())
        val repository = ScreenRepository(
            remote = stubRemote { schema },
            coroutineScope = repoScope
        )

        repository.streamScreen("main", refresh = false)
        assertEquals(ScreenResult.Idle, repository.getCurrent("main"))

        repository.refresh("main")
        repoScope.advanceUntilIdle()
        advanceUntilIdle()

        val success = repository.getCurrent("main") as ScreenResult.Success
        assertEquals(schema, success.schema)
        assertTrue(success.receivedAt > 0)
        repoScope.cancel()
    }

    @Test
    fun `streamScreen emits error on failure`() = runTest {
        val repoScope = TestScope(StandardTestDispatcher())
        val repository = ScreenRepository(
            remote = stubRemote { error("boom") },
            coroutineScope = repoScope
        )

        repository.streamScreen("main", refresh = false)
        assertEquals(ScreenResult.Idle, repository.getCurrent("main"))

        repository.refresh("main")
        repoScope.advanceUntilIdle()
        advanceUntilIdle()

        val error = repository.getCurrent("main") as ScreenResult.Error
        assertTrue(error.throwable is IllegalStateException)
        repoScope.cancel()
    }

    private fun sampleSchema(id: String) = ScreenSchema(
        document = DocumentMeta(
            documentId = "document-$id",
            name = "Document $id",
            exportedAt = "2025-01-01T00:00:00Z"
        ),
        screen = ScreenDefinition(
            id = id,
            type = "screen",
            name = "Screen $id",
            sections = ScreenSections(
                topBar = SchemaNode(type = "text", content = JsonPrimitive("Top")),
                body = SchemaNode(
                    type = "column",
                    children = listOf(
                        SchemaNode(type = "text", content = JsonPrimitive("Hello")),
                        SchemaNode(type = "button", content = JsonPrimitive("Action"))
                    )
                )
            )
        )
    )

    private fun stubRemote(block: suspend (String) -> ScreenSchema) = object : RemoteDataSource {
        override suspend fun fetchScreen(screenId: String): ScreenSchema = block(screenId)
    }
}
