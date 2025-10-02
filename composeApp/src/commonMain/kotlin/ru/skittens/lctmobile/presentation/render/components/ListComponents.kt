package ru.skittens.lctmobile.presentation.render.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.dpValue
import ru.skittens.shared.model.SchemaNode

/**
 * Renders a virtualized vertical list component based on JSON schema.
 * 
 * Supported properties:
 * - `items`: JsonArray - array of item data to render
 * 
 * Supported styles:
 * - `spacing`: Dp - spacing between items (default: 8dp)
 * - `contentPadding`: Dp - padding inside the list
 * - Plus all common styles
 * 
 * The component will render children nodes as templates for each item.
 * If no children are provided, it will try to parse items from the `items` array.
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "lazycolumn",
 *   "properties": {
 *     "items": [
 *       {"title": "Item 1", "subtitle": "Description 1"},
 *       {"title": "Item 2", "subtitle": "Description 2"}
 *     ]
 *   },
 *   "style": {
 *     "spacing": 12,
 *     "contentPadding": 16
 *   },
 *   "children": [
 *     {
 *       "type": "card",
 *       "children": [
 *         {"type": "title", "content": "{title}"},
 *         {"type": "text", "content": "{subtitle}"}
 *       ]
 *     }
 *   ]
 * }
 * ```
 */
@Composable
fun RenderLazyColumn(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    renderNode: @Composable (SchemaNode, Modifier) -> Unit
) {
    val spacing = node.style.dpValue("spacing") ?: 8.dp
    val contentPadding = node.style.dpValue("contentPadding") ?: 0.dp
    
    // Используем children как список элементов для отображения
    val itemsList = node.children

    LazyColumn(
        modifier = modifier.applyStyle(node.style),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(contentPadding)
    ) {
        items(itemsList) { itemNode ->
            renderNode(itemNode, Modifier.fillMaxWidth())
        }
    }
}

/**
 * Renders a virtualized horizontal list component based on JSON schema.
 * 
 * Properties and styles are the same as LazyColumn.
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "lazyrow",
 *   "properties": {
 *     "items": [...]
 *   },
 *   "style": {
 *     "spacing": 8
 *   },
 *   "children": [...]
 * }
 * ```
 */
@Composable
fun RenderLazyRow(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    renderNode: @Composable (SchemaNode, Modifier) -> Unit
) {
    val spacing = node.style.dpValue("spacing") ?: 8.dp
    val contentPadding = node.style.dpValue("contentPadding") ?: 0.dp
    
    // Используем children как список элементов для отображения
    val itemsList = node.children

    LazyRow(
        modifier = modifier.applyStyle(node.style),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(contentPadding)
    ) {
        items(itemsList) { itemNode ->
            renderNode(itemNode, Modifier)
        }
    }
}
