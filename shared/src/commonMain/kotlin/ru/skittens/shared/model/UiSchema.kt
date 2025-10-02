package ru.skittens.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class ScreenSchema(
    val document: DocumentMeta,
    val screen: ScreenDefinition
)

@Serializable
data class DocumentMeta(
    val documentId: String,
    val name: String,
    val exportedAt: String
)

@Serializable
data class ScreenDefinition(
    val id: String,
    val type: String,
    val name: String? = null,
    val style: JsonObject = EMPTY_OBJECT,
    val sections: ScreenSections = ScreenSections(),
    val references: JsonObject = EMPTY_OBJECT
)

@Serializable
data class ScreenSections(
    val topBar: SchemaNode? = null,
    val body: SchemaNode? = null,
    val bottomBar: SchemaNode? = null
)

@Serializable
data class SchemaNode(
    val id: String? = null,
    val type: String,
    val name: String? = null,
    val properties: JsonObject = EMPTY_OBJECT,
    val style: JsonObject = EMPTY_OBJECT,
    val children: List<SchemaNode> = emptyList(),
    val position: Position? = null,
    val data: JsonPrimitive? = null,
    val dataSource: String? = null,
    val content: JsonPrimitive? = null,
    val items: JsonArray? = null,
    val action: JsonObject? = null
)

@Serializable
data class Position(
    val x: Int? = null,
    val y: Int? = null
)

private val EMPTY_OBJECT = JsonObject(emptyMap())
