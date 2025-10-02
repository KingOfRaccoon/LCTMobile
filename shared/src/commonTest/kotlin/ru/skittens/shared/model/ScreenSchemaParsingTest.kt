package ru.skittens.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import ru.skittens.shared.util.SharedJson

class ScreenSchemaParsingTest {
    @Test
    fun `decode schema with sections and nodes`() {
        val json = """
            {
              "document": {
                "documentId": "demo",
                "name": "Demo",
                "exportedAt": "2025-01-01T00:00:00Z"
              },
              "screen": {
                "id": "main",
                "type": "screen",
                "name": "Главная",
                "sections": {
                  "topBar": {
                    "type": "text",
                    "content": "Title"
                  },
                  "body": {
                    "type": "column",
                    "children": [
                      {"type": "text", "content": "Welcome"},
                      {"type": "button", "content": "Refresh"}
                    ]
                  }
                },
                "references": {}
              }
            }
        """.trimIndent()

        val schema = SharedJson.decodeFromString<ScreenSchema>(json)

        assertEquals("demo", schema.document.documentId)
        assertEquals("main", schema.screen.id)
        val body = schema.screen.sections.body
        assertNotNull(body)
        assertEquals("column", body.type)
        assertEquals(2, body.children.size)
        assertEquals("text", body.children[0].type)
        assertEquals("button", body.children[1].type)
    }
}
