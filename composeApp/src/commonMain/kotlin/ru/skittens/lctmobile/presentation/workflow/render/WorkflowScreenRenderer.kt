package ru.skittens.lctmobile.presentation.workflow.render

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ru.skittens.shared.model.workflow.*

/**
 * Renderer для динамической генерации Compose UI из ScreenData (Workflow Engine)
 * Отличается от BackendDrivenRenderer - работает с другим форматом данных
 */
@Composable
fun WorkflowScreenRenderer(
    screenData: ScreenData,
    context: Map<String, Any> = emptyMap(),
    onEvent: (eventName: String, data: Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier
) {
    var formState by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        screenData.title?.let { title ->
            Text(
                text = title.interpolate(context),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Описание
        screenData.description?.let { description ->
            Text(
                text = description.interpolate(context),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Поля формы
        screenData.fields?.forEach { field ->
            RenderField(
                field = field,
                value = formState[field.id],
                onValueChange = { value ->
                    formState = formState + (field.id to value)
                }
            )
        }

        // Компоненты
        screenData.components?.forEach { component ->
            RenderComponent(
                component = component,
                context = context + formState,
                onEvent = onEvent
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопки
        screenData.buttons?.forEach { button ->
            val isEnabled = button.enabled?.let { condition ->
                evaluateCondition(condition, context + formState)
            } ?: true

            Button(
                onClick = {
                    onEvent(button.event, formState)
                },
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(button.label)
            }
        }
    }
}

/**
 * Рендер поля ввода
 */
@Composable
private fun RenderField(
    field: FieldData,
    value: Any?,
    onValueChange: (Any) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (field.type) {
            "email", "text" -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { onValueChange(it) },
                    label = { Text(field.label) },
                    placeholder = field.placeholder?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (field.type == "email") KeyboardType.Email else KeyboardType.Text
                    ),
                    isError = field.required && (value == null || value.toString().isEmpty())
                )
            }

            "password" -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { onValueChange(it) },
                    label = { Text(field.label) },
                    placeholder = field.placeholder?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = field.required && (value == null || value.toString().isEmpty())
                )
            }

            "number" -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { text ->
                        onValueChange(text.toIntOrNull() ?: 0)
                    },
                    label = { Text(field.label) },
                    placeholder = field.placeholder?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            "phone" -> {
                OutlinedTextField(
                    value = value?.toString() ?: "",
                    onValueChange = { onValueChange(it) },
                    label = { Text(field.label) },
                    placeholder = field.placeholder?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            "checkbox" -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = value as? Boolean ?: false,
                        onCheckedChange = { onValueChange(it) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(field.label)
                        field.link?.let { link ->
                            TextButton(onClick = { /* TODO: open link */ }) {
                                Text(link.text, style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                }
            }

            else -> {
                Text("Unsupported field type: ${field.type}")
            }
        }

        // Validation error
        field.validation?.errorMessage?.let { errorMsg ->
            if (field.required && (value == null || value.toString().isEmpty())) {
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

/**
 * Рендер компонента
 */
@Composable
private fun RenderComponent(
    component: ComponentData,
    context: Map<String, Any>,
    onEvent: (String, Map<String, Any>) -> Unit
) {
    when (component.type) {
        "text" -> {
            val textStyle = when (component.style) {
                "heading" -> MaterialTheme.typography.h6
                "body" -> MaterialTheme.typography.body1
                "muted" -> MaterialTheme.typography.caption
                else -> MaterialTheme.typography.body1
            }

            component.content?.let { content ->
                Text(
                    text = content.interpolate(context),
                    style = textStyle
                )
            }
        }

        "card_list" -> {
            component.items?.forEach { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = if (card.highlighted) 8.dp else 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(card.title, style = MaterialTheme.typography.h6)
                            card.badge?.let { badge ->
                                Surface(
                                    color = MaterialTheme.colors.primary,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = badge,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                            }
                        }

                        card.price?.let { price ->
                            Text(
                                text = price,
                                style = MaterialTheme.typography.h5,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        card.features?.forEach { feature ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", style = MaterialTheme.typography.body2)
                                Text(feature, style = MaterialTheme.typography.body2)
                            }
                        }

                        card.action?.let { action ->
                            Button(
                                onClick = {
                                    val params = action.params.mapValues { (_, jsonElement) ->
                                        jsonElement.toString()
                                    }
                                    onEvent(action.event, params)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Выбрать")
                            }
                        }
                    }
                }
            }
        }

        "status_badge" -> {
            component.status?.let { status ->
                val statusStyle = component.statusMap?.get(status)
                statusStyle?.let { style ->
                    Surface(
                        color = when (style.color) {
                            "green" -> MaterialTheme.colors.primary
                            "yellow" -> MaterialTheme.colors.secondary
                            "red" -> MaterialTheme.colors.error
                            else -> MaterialTheme.colors.surface
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = style.label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }

        "progress_bar" -> {
            component.value?.let { valueStr ->
                val progress = valueStr.toFloatOrNull() ?: 0f
                val maxValue = component.max?.toFloat() ?: 100f

                Column {
                    LinearProgressIndicator(
                        progress = progress / maxValue,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Заполнено ${progress.toInt()}%",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        "conditional" -> {
            component.condition?.let { condition ->
                val conditionMet = evaluateCondition(condition, context)
                if (conditionMet) {
                    component.ifTrue?.let { trueComponent ->
                        RenderComponent(trueComponent, context, onEvent)
                    }
                } else {
                    component.ifFalse?.let { falseComponent ->
                        RenderComponent(falseComponent, context, onEvent)
                    }
                }
            }
        }

        else -> {
            Text("Unsupported component type: ${component.type}")
        }
    }
}

/**
 * Интерполяция переменных {{variable}} в тексте
 */
private fun String.interpolate(context: Map<String, Any>): String {
    var result = this
    val regex = """\{\{(\w+)\}\}""".toRegex()

    regex.findAll(this).forEach { match ->
        val variableName = match.groupValues[1]
        val value = context[variableName]?.toString() ?: ""
        result = result.replace(match.value, value)
    }

    return result
}

/**
 * Простая оценка условий (заглушка, нужна полноценная реализация)
 */
private fun evaluateCondition(condition: String, context: Map<String, Any>): Boolean {
    // Базовая реализация для условий вида "{{variable}} == 'value'"
    val interpolated = condition.interpolate(context)

    return when {
        interpolated.contains("==") -> {
            val parts = interpolated.split("==").map { it.trim().trim('\'', '"') }
            parts[0] == parts[1]
        }
        interpolated == "true" -> true
        interpolated == "false" -> false
        else -> true
    }
}
