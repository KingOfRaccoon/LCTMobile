package ru.skittens.lctmobile.presentation.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.contentOrNull
import ru.skittens.lctmobile.presentation.state.BackendDrivenUiState
import ru.skittens.lctmobile.util.applyStyle
import ru.skittens.lctmobile.util.dpValue
import ru.skittens.lctmobile.util.floatValue
import ru.skittens.lctmobile.util.parseColor
import ru.skittens.lctmobile.util.stringValue
import ru.skittens.shared.model.SchemaNode
import ru.skittens.shared.model.ScreenSchema
import ru.skittens.lctmobile.presentation.render.components.*
import ru.skittens.lctmobile.presentation.action.ActionHandler
import ru.skittens.shared.action.ActionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BackendDrivenScreen(
    uiState: BackendDrivenUiState,
    actionHandler: ActionHandler,
    modifier: Modifier = Modifier
) {
    val schema = uiState.schema
    if (schema == null) {
        Placeholder(uiState = uiState, modifier = modifier)
    } else {
        RenderScreen(schema, actionHandler, modifier.fillMaxSize())
    }
}

@Composable
private fun Placeholder(uiState: BackendDrivenUiState, modifier: Modifier) {
    if (uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        Text(
            text = uiState.error ?: "Нет данных",
            modifier = modifier.fillMaxSize().padding(24.dp)
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun RenderScreen(schema: ScreenSchema, actionHandler: ActionHandler, modifier: Modifier) {
    val definition = schema.screen
    Column(modifier = modifier) {
        definition.sections.topBar?.let {
            RenderNode(it, actionHandler, Modifier.fillMaxWidth())
        }
        definition.sections.body?.let {
            Box(modifier = Modifier.weight(1f, fill = true).fillMaxWidth()) {
                RenderNode(it, actionHandler, Modifier.fillMaxSize())
            }
        }
        definition.sections.bottomBar?.let {
            RenderNode(it, actionHandler, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RenderNode(node: SchemaNode, actionHandler: ActionHandler, modifier: Modifier = Modifier) {
    // Helper function to handle actions
    val handleAction: () -> Unit = {
        node.action?.let { actionJson ->
            ActionParser.parse(actionJson)?.let { action ->
                CoroutineScope(Dispatchers.Main).launch {
                    actionHandler.handle(action) { result ->
                        result.onFailure { error ->
                            println("Action failed: ${error.message}")
                        }
                    }
                }
            }
        }
    }
    
    when (node.type.lowercase()) {
        // Layout containers
        "column", "container", "section", "body" -> RenderColumn(node, actionHandler, modifier)
        "row" -> RenderRow(node, actionHandler, modifier)
        "box" -> RenderBox(node, modifier) { child, childModifier -> RenderNode(child, actionHandler, childModifier) }
        "surface" -> RenderSurface(node, modifier) { child, childModifier -> RenderNode(child, actionHandler, childModifier) }
        
        // Text components
        "text", "title", "subtitle", "label" -> RenderText(node, modifier)
        
        // Interactive components
        "button" -> RenderButton(node, modifier, handleAction)
        "textfield", "textinput", "input" -> RenderTextField(node, modifier)
        "checkbox" -> RenderCheckbox(node, modifier)
        "switch", "toggle" -> RenderSwitch(node, modifier)
        "dropdown", "select" -> RenderDropdown(node, modifier)
        
        // Display components
        "card" -> RenderCard(node, modifier, handleAction) { child, childModifier -> RenderNode(child, actionHandler, childModifier) }
        "divider" -> RenderDivider(node, modifier)
        "image" -> RenderImage(node, modifier)
        
        // Progress indicators
        "linearprogress", "progressbar" -> RenderLinearProgress(node, modifier)
        "circularprogress", "loader" -> RenderCircularProgress(node, modifier)
        
        // List components
        "lazycolumn", "list" -> RenderLazyColumn(node, modifier) { child, childModifier -> RenderNode(child, actionHandler, childModifier) }
        "lazyrow", "horizontallist" -> RenderLazyRow(node, modifier) { child, childModifier -> RenderNode(child, actionHandler, childModifier) }
        
        // Utility
        "spacer" -> RenderSpacer(node, modifier)
        
        // Fallback
        else -> RenderFallback(node, actionHandler, modifier)
    }
}

@Composable
private fun RenderColumn(node: SchemaNode, actionHandler: ActionHandler, modifier: Modifier) {
    val spacing = node.resolveDp("spacing") ?: 0.dp
    val horizontalAlignment = when (node.resolveString("horizontalAlignment")?.lowercase()) {
        "center" -> Alignment.CenterHorizontally
        "end", "right" -> Alignment.End
        else -> Alignment.Start
    }
    Column(
        modifier = modifier.applyStyle(node.style),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = horizontalAlignment
    ) {
        node.children.forEach { child ->
            RenderNode(child, actionHandler, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RenderRow(node: SchemaNode, actionHandler: ActionHandler, modifier: Modifier) {
    val spacing = node.resolveDp("spacing") ?: 0.dp
    val verticalAlignment = when (node.resolveString("verticalAlignment")?.lowercase()) {
        "center" -> Alignment.CenterVertically
        "end", "bottom" -> Alignment.Bottom
        else -> Alignment.Top
    }
    Row(
        modifier = modifier.applyStyle(node.style),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = verticalAlignment
    ) {
        node.children.forEach { child ->
            RenderNode(child, actionHandler)
        }
    }
}

@Composable
private fun RenderText(node: SchemaNode, modifier: Modifier) {
    val text = node.textContent()
    val color = node.style.stringValue("color")?.let(::parseColor)
    val style = when (node.type.lowercase()) {
        "title" -> MaterialTheme.typography.headlineSmall
        "subtitle" -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.bodyLarge
    }
    Text(
        text = text,
        modifier = modifier.applyStyle(node.style),
        color = color ?: Color.Unspecified,
        style = style,
        maxLines = node.properties.stringValue("maxLines")?.toIntOrNull() ?: Int.MAX_VALUE,
        lineHeight = style.lineHeight,
        fontSize = node.resolveFloat("fontSize")?.sp ?: style.fontSize
    )
}

@Composable
private fun RenderButton(node: SchemaNode, modifier: Modifier, onClick: () -> Unit = {}) {
    val label = node.textContent(ifEmpty = "Кнопка")
    Button(
        onClick = onClick,
        modifier = modifier.applyStyle(node.style)
    ) {
        Text(label)
    }
}

@Composable
private fun RenderImage(node: SchemaNode, modifier: Modifier) {
    val description = node.textContent(ifEmpty = node.resolveString("description") ?: "Изображение")
    val source = node.resolveString("url") ?: node.resolveString("src")
    val text = buildString {
        append("[Изображение]")
        source?.let { append(" ").append(it) }
        if (description.isNotBlank()) {
            append(" — ").append(description)
        }
    }
    Text(
        text = text,
        modifier = modifier.applyStyle(node.style),
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
private fun RenderSpacer(node: SchemaNode, modifier: Modifier) {
    val height = node.resolveDp("height") ?: 8.dp
    Spacer(modifier = modifier.height(height))
}

@Composable
private fun RenderFallback(node: SchemaNode, actionHandler: ActionHandler, modifier: Modifier) {
    if (node.children.isEmpty()) {
        val text = node.textContent(ifEmpty = node.type)
        Text(
            text = text,
            modifier = modifier.applyStyle(node.style),
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Column(modifier = modifier.applyStyle(node.style)) {
            node.children.forEach { child ->
                RenderNode(child, actionHandler, Modifier.fillMaxWidth())
            }
        }
    }
}

private fun SchemaNode.textContent(ifEmpty: String = ""): String {
    return content?.contentOrNull
        ?: data?.contentOrNull
        ?: properties.stringValue("text")
        ?: properties.stringValue("title")
        ?: properties.stringValue("label")
        ?: ifEmpty
}

private fun SchemaNode.resolveString(key: String): String? =
    properties.stringValue(key) ?: style.stringValue(key)

private fun SchemaNode.resolveFloat(key: String): Float? =
    properties.floatValue(key) ?: style.floatValue(key)

private fun SchemaNode.resolveDp(key: String): Dp? =
    properties.dpValue(key) ?: style.dpValue(key)

