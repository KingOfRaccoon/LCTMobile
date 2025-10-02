package ru.skittens.lctmobile.presentation.render.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.booleanValue
import ru.skittens.lctmobile.util.stringValue
import ru.skittens.shared.model.SchemaNode

/**
 * Renders a text input field component based on JSON schema.
 * 
 * Supported properties:
 * - `placeholder`: String - placeholder text
 * - `value`: String - initial/current value
 * - `label`: String - field label
 * - `multiline`: Boolean - enable multiline input
 * - `maxLines`: Int - maximum lines for multiline mode
 * - `readOnly`: Boolean - make field read-only
 * - `outlined`: Boolean - use outlined style (default: true)
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "textfield",
 *   "properties": {
 *     "placeholder": "Enter your name",
 *     "label": "Name",
 *     "value": "",
 *     "multiline": false
 *   },
 *   "style": {
 *     "padding": 16
 *   }
 * }
 * ```
 */
@Composable
fun RenderTextField(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {}
) {
    val placeholder = node.properties.stringValue("placeholder") ?: ""
    val initialValue = node.properties.stringValue("value") ?: ""
    val label = node.properties.stringValue("label")
    val multiline = node.properties.booleanValue("multiline") ?: false
    val maxLines = node.properties.stringValue("maxLines")?.toIntOrNull() ?: if (multiline) Int.MAX_VALUE else 1
    val readOnly = node.properties.booleanValue("readOnly") ?: false
    val outlined = node.properties.booleanValue("outlined") ?: true

    var textState by remember(node.id) { mutableStateOf(initialValue) }

    val textFieldModifier = modifier
        .applyStyle(node.style)
        .fillMaxWidth()

    if (outlined) {
        OutlinedTextField(
            value = textState,
            onValueChange = { newValue ->
                textState = newValue
                onValueChange(newValue)
            },
            modifier = textFieldModifier,
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            label = label?.let { { Text(it) } },
            readOnly = readOnly,
            singleLine = !multiline,
            maxLines = maxLines
        )
    } else {
        TextField(
            value = textState,
            onValueChange = { newValue ->
                textState = newValue
                onValueChange(newValue)
            },
            modifier = textFieldModifier,
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            label = label?.let { { Text(it) } },
            readOnly = readOnly,
            singleLine = !multiline,
            maxLines = maxLines
        )
    }
}
