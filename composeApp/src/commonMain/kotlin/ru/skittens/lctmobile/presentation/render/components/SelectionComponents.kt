package ru.skittens.lctmobile.presentation.render.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.booleanValue
import ru.skittens.lctmobile.util.stringValue
import ru.skittens.shared.model.SchemaNode

/**
 * Renders a checkbox component based on JSON schema.
 * 
 * Supported properties:
 * - `checked`: Boolean - initial checked state (default: false)
 * - `label`: String - text label next to checkbox
 * - `enabled`: Boolean - whether checkbox is enabled (default: true)
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "checkbox",
 *   "properties": {
 *     "checked": false,
 *     "label": "I agree to terms",
 *     "enabled": true
 *   },
 *   "style": {
 *     "padding": 8
 *   }
 * }
 * ```
 */
@Composable
fun RenderCheckbox(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val initialChecked = node.properties.booleanValue("checked") ?: false
    val label = node.properties.stringValue("label")
    val enabled = node.properties.booleanValue("enabled") ?: true

    var checkedState by remember(node.id) { mutableStateOf(initialChecked) }

    Row(
        modifier = modifier.applyStyle(node.style),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = { newValue ->
                checkedState = newValue
                onCheckedChange(newValue)
            },
            enabled = enabled
        )
        if (label != null) {
            Spacer(Modifier.width(8.dp))
            Text(text = label)
        }
    }
}

/**
 * Renders a switch/toggle component based on JSON schema.
 * 
 * Supported properties:
 * - `checked`: Boolean - initial checked state (default: false)
 * - `label`: String - text label next to switch
 * - `enabled`: Boolean - whether switch is enabled (default: true)
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "switch",
 *   "properties": {
 *     "checked": true,
 *     "label": "Enable notifications",
 *     "enabled": true
 *   },
 *   "style": {
 *     "padding": 8
 *   }
 * }
 * ```
 */
@Composable
fun RenderSwitch(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val initialChecked = node.properties.booleanValue("checked") ?: false
    val label = node.properties.stringValue("label")
    val enabled = node.properties.booleanValue("enabled") ?: true

    var checkedState by remember(node.id) { mutableStateOf(initialChecked) }

    Row(
        modifier = modifier.applyStyle(node.style),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(text = label)
            Spacer(Modifier.width(8.dp))
        }
        Switch(
            checked = checkedState,
            onCheckedChange = { newValue ->
                checkedState = newValue
                onCheckedChange(newValue)
            },
            enabled = enabled
        )
    }
}
