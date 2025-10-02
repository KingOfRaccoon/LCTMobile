package ru.skittens.lctmobile.presentation.render.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.floatValue
import ru.skittens.shared.model.SchemaNode

/**
 * Renders a linear progress indicator based on JSON schema.
 * 
 * Supported properties:
 * - `progress`: Float - progress value (0.0 to 1.0), if null shows indeterminate
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "linearprogress",
 *   "properties": {
 *     "progress": 0.75
 *   }
 * }
 * ```
 */
@Composable
fun RenderLinearProgress(
    node: SchemaNode,
    modifier: Modifier = Modifier
) {
    val progress = node.properties.floatValue("progress")
    
    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier
                .fillMaxWidth()
                .applyStyle(node.style)
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .applyStyle(node.style)
        )
    }
}

/**
 * Renders a circular progress indicator based on JSON schema.
 * 
 * Supported properties:
 * - `progress`: Float - progress value (0.0 to 1.0), if null shows indeterminate
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "circularprogress",
 *   "properties": {
 *     "progress": 0.5
 *   }
 * }
 * ```
 */
@Composable
fun RenderCircularProgress(
    node: SchemaNode,
    modifier: Modifier = Modifier
) {
    val progress = node.properties.floatValue("progress")
    
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier.applyStyle(node.style)
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier.applyStyle(node.style)
        )
    }
}
