package ru.skittens.lctmobile.presentation.render.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.booleanValue
import ru.skittens.lctmobile.util.dpValue
import ru.skittens.shared.model.SchemaNode

/**
 * Renders a card container component based on JSON schema.
 * 
 * Supported properties:
 * - `clickable`: Boolean - whether card is clickable (default: false)
 * 
 * Supported styles:
 * - `elevation`: Dp - card elevation (default: 2dp)
 * - Plus all common styles (padding, backgroundColor, etc.)
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "card",
 *   "properties": {
 *     "clickable": true
 *   },
 *   "style": {
 *     "elevation": 4,
 *     "padding": 16
 *   },
 *   "children": [
 *     {
 *       "type": "title",
 *       "content": "Card Title"
 *     },
 *     {
 *       "type": "text",
 *       "content": "Card content goes here"
 *     }
 *   ]
 * }
 * ```
 */
@Composable
fun RenderCard(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    renderNode: @Composable (SchemaNode, Modifier) -> Unit
) {
    val clickable = node.properties.booleanValue("clickable") ?: false
    val elevation = node.style.dpValue("elevation") ?: 2.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (clickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .applyStyle(node.style),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            node.children.forEach { child ->
                renderNode(child, Modifier.fillMaxWidth())
            }
        }
    }
}
