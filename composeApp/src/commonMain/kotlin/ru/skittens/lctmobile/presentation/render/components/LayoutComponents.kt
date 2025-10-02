package ru.skittens.lctmobile.presentation.render.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.dpValue
import ru.skittens.lctmobile.util.parseColor
import ru.skittens.lctmobile.util.stringValue
import ru.skittens.shared.model.SchemaNode

/**
 * Renders a horizontal divider based on JSON schema.
 * 
 * Supported styles:
 * - `thickness`: Dp - divider thickness (default: 1dp)
 * - `color`: String - divider color in HEX format
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "divider",
 *   "style": {
 *     "thickness": 2,
 *     "color": "#E0E0E0"
 *   }
 * }
 * ```
 */
@Composable
fun RenderDivider(
    node: SchemaNode,
    modifier: Modifier = Modifier
) {
    val thickness = node.style.dpValue("thickness") ?: 1.dp
    val color = node.style.stringValue("color")?.let(::parseColor)
    
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .applyStyle(node.style),
        thickness = thickness,
        color = color ?: androidx.compose.ui.graphics.Color.LightGray
    )
}

/**
 * Renders a surface container based on JSON schema.
 * Surface provides elevation and background.
 * 
 * Supported styles:
 * - `elevation`: Dp - surface elevation (default: 0dp)
 * - Plus all common styles
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "surface",
 *   "style": {
 *     "elevation": 4,
 *     "padding": 16
 *   },
 *   "children": [...]
 * }
 * ```
 */
@Composable
fun RenderSurface(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    renderNode: @Composable (SchemaNode, Modifier) -> Unit
) {
    val elevation = node.style.dpValue("elevation") ?: 0.dp
    
    Surface(
        modifier = modifier.applyStyle(node.style),
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Box {
            node.children.forEach { child ->
                renderNode(child, Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * Renders a box container based on JSON schema.
 * Box allows overlapping children.
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "box",
 *   "style": {
 *     "padding": 16
 *   },
 *   "children": [...]
 * }
 * ```
 */
@Composable
fun RenderBox(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    renderNode: @Composable (SchemaNode, Modifier) -> Unit
) {
    Box(
        modifier = modifier.applyStyle(node.style)
    ) {
        node.children.forEach { child ->
            renderNode(child, Modifier)
        }
    }
}
