package ru.skittens.lctmobile.presentation.render.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.stringValue
import ru.skittens.shared.model.SchemaNode

/**
 * Renders a dropdown/select component based on JSON schema.
 * 
 * Supported properties:
 * - `options`: JsonArray - array of options (strings or objects with "label" and "value")
 * - `selected`: String - initially selected value
 * - `label`: String - dropdown label
 * - `placeholder`: String - placeholder text when nothing selected
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "dropdown",
 *   "properties": {
 *     "label": "Select country",
 *     "placeholder": "Choose...",
 *     "selected": "us",
 *     "options": [
 *       {"label": "United States", "value": "us"},
 *       {"label": "Canada", "value": "ca"},
 *       {"label": "Mexico", "value": "mx"}
 *     ]
 *   }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderDropdown(
    node: SchemaNode,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {}
) {
    val label = node.properties.stringValue("label")
    val placeholder = node.properties.stringValue("placeholder") ?: "Select..."
    val initialSelected = node.properties.stringValue("selected") ?: ""
    
    // Parse options from JSON array
    val options = remember(node.properties) {
        val optionsArray = node.properties["options"] as? JsonArray
        optionsArray?.mapNotNull { element ->
            when (element) {
                is JsonPrimitive -> element.content to element.content
                else -> {
                    val label = (element as? kotlinx.serialization.json.JsonObject)
                        ?.get("label")?.toString()?.trim('"')
                    val value = (element as? kotlinx.serialization.json.JsonObject)
                        ?.get("value")?.toString()?.trim('"')
                    if (label != null && value != null) label to value else null
                }
            }
        } ?: emptyList()
    }
    
    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember(node.id) { mutableStateOf(initialSelected) }
    val selectedLabel = options.find { it.second == selectedValue }?.first ?: placeholder
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.applyStyle(node.style)
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = label?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (optionLabel, optionValue) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        selectedValue = optionValue
                        expanded = false
                        onValueChange(optionValue)
                    }
                )
            }
        }
    }
}
