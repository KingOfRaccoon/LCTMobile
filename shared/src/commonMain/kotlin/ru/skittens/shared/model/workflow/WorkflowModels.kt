package ru.skittens.shared.model.workflow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Тип состояния в workflow FSM
 */
@Serializable
enum class StateType {
    @SerialName("technical")
    TECHNICAL,      // Выполнение Python-выражений, обновление контекста

    @SerialName("integration")
    INTEGRATION,    // Вызов внешних API

    @SerialName("screen")
    SCREEN,         // UI-состояние, управляется событиями

    @SerialName("service")
    SERVICE         // Системные состояния (__init__, __error__)
}

/**
 * Переход между состояниями
 */
@Serializable
data class Transition(
    @SerialName("target_state")
    val targetState: String,

    val condition: String? = null,

    @SerialName("event_name")
    val eventName: String? = null
)

/**
 * Выражение для вычисления переменных (Technical State)
 */
@Serializable
data class Expression(
    val variable: String,

    @SerialName("dependent_variables")
    val dependentVariables: List<String> = emptyList(),

    val expression: String
)

/**
 * Конфигурация интеграции с внешним API (Integration State)
 */
@Serializable
data class IntegrationConfig(
    val variable: String,
    val url: String,
    val params: Map<String, String> = emptyMap(),
    val method: String = "get"
)

/**
 * Определение состояния в workflow
 */
@Serializable
data class StateModel(
    @SerialName("state_type")
    val stateType: StateType,

    val name: String,

    val transitions: List<Transition> = emptyList(),

    val expressions: List<Expression> = emptyList(),

    @SerialName("integration_config")
    val integrationConfig: IntegrationConfig? = null,

    @SerialName("initial_state")
    val initialState: Boolean = false,

    @SerialName("final_state")
    val finalState: Boolean = false
)

/**
 * Набор состояний workflow
 */
@Serializable
data class StateSet(
    val states: List<StateModel>
)

/**
 * Запрос на сохранение workflow
 */
@Serializable
data class SaveWorkflowRequest(
    val states: StateSet,

    @SerialName("predefined_context")
    val predefinedContext: Map<String, JsonElement> = emptyMap()
)

/**
 * Ответ на сохранение workflow
 */
@Serializable
data class SaveWorkflowResponse(
    val status: String,

    @SerialName("wf_description_id")
    val wfDescriptionId: String,

    @SerialName("wf_context_id")
    val wfContextId: String
)

/**
 * Запрос на выполнение workflow
 */
@Serializable
data class WorkflowExecutionRequest(
    @SerialName("client_session_id")
    val clientSessionId: String,

    @SerialName("client_workflow_id")
    val clientWorkflowId: String? = null,

    val context: Map<String, JsonElement> = emptyMap(),

    @SerialName("event_name")
    val eventName: String? = null
)

/**
 * Ответ на выполнение workflow
 */
@Serializable
data class WorkflowExecutionResponse(
    @SerialName("session_id")
    val sessionId: String,

    val context: Map<String, JsonElement>,

    @SerialName("current_state")
    val currentState: String,

    @SerialName("state_type")
    val stateType: StateType,

    val screen: ScreenData? = null
)

/**
 * Данные экрана для отображения (Screen State)
 */
@Serializable
data class ScreenData(
    val title: String? = null,

    val description: String? = null,

    val fields: List<FieldData>? = null,

    val buttons: List<ButtonData>? = null,

    val components: List<ComponentData>? = null
)

/**
 * Поле ввода на экране
 */
@Serializable
data class FieldData(
    val id: String,

    val type: String, // "text", "email", "password", "number", "date", "phone", "checkbox"

    val label: String,

    val placeholder: String? = null,

    val required: Boolean = false,

    val validation: ValidationConfig? = null,

    val mask: String? = null,

    val link: LinkData? = null
)

/**
 * Конфигурация валидации поля
 */
@Serializable
data class ValidationConfig(
    @SerialName("minLength")
    val minLength: Int? = null,

    @SerialName("maxLength")
    val maxLength: Int? = null,

    val pattern: String? = null,

    @SerialName("errorMessage")
    val errorMessage: String? = null,

    @SerialName("minAge")
    val minAge: Int? = null,

    @SerialName("maxAge")
    val maxAge: Int? = null
)

/**
 * Ссылка (для checkbox terms и т.д.)
 */
@Serializable
data class LinkData(
    val text: String,
    val url: String
)

/**
 * Кнопка на экране
 */
@Serializable
data class ButtonData(
    val id: String,

    val label: String,

    val event: String,

    val style: String = "primary", // "primary", "secondary", "text"

    val enabled: String? = null // Условие (может содержать {{variables}})
)

/**
 * Компонент UI (для информационных экранов)
 */
@Serializable
data class ComponentData(
    val type: String, // "text", "card_list", "status_badge", "progress_bar", "conditional", "image"

    val content: String? = null,

    val style: String? = null,

    val url: String? = null,

    val items: List<CardItem>? = null,

    val status: String? = null,

    @SerialName("statusMap")
    val statusMap: Map<String, StatusStyle>? = null,

    val value: String? = null,

    val max: Int? = null,

    val condition: String? = null,

    @SerialName("ifTrue")
    val ifTrue: ComponentData? = null,

    @SerialName("ifFalse")
    val ifFalse: ComponentData? = null
)

/**
 * Элемент карточки (для card_list)
 */
@Serializable
data class CardItem(
    val id: String,
    val title: String,
    val price: String? = null,
    val features: List<String>? = null,
    val badge: String? = null,
    val highlighted: Boolean = false,
    val action: ActionData? = null
)

/**
 * Действие (для кнопок в карточках и т.д.)
 */
@Serializable
data class ActionData(
    val event: String,
    val params: Map<String, JsonElement> = emptyMap()
)

/**
 * Стиль статуса (для status_badge)
 */
@Serializable
data class StatusStyle(
    val label: String,
    val color: String
)

/**
 * Ответ healthcheck
 */
@Serializable
data class HealthCheckResponse(
    val status: String
)
