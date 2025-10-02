package ru.skittens.shared.action

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents UI actions that can be triggered from the backend-driven UI.
 * Actions are parsed from JSON schema and executed by the ActionHandler.
 */
@Serializable
sealed class UiAction {
    
    /**
     * Navigate to another screen.
     * @param screenId Target screen identifier
     * @param clearStack If true, clears the navigation stack
     */
    @Serializable
    data class Navigate(
        val screenId: String,
        val clearStack: Boolean = false
    ) : UiAction()
    
    /**
     * Navigate back in the navigation stack.
     */
    @Serializable
    data object NavigateBack : UiAction()
    
    /**
     * Open an external URL (browser, deep link).
     * @param url External URL to open
     */
    @Serializable
    data class NavigateExternal(
        val url: String
    ) : UiAction()
    
    /**
     * Make an API call to the backend.
     * @param endpoint API endpoint path
     * @param method HTTP method (GET, POST, PUT, DELETE)
     * @param body Optional request body
     * @param onSuccess Optional action to execute on success
     * @param onError Optional action to execute on error
     */
    @Serializable
    data class ApiCall(
        val endpoint: String,
        val method: String = "GET",
        val body: JsonObject? = null,
        val onSuccess: String? = null,
        val onError: String? = null
    ) : UiAction()
    
    /**
     * Update local state value.
     * @param key State key
     * @param value New value
     */
    @Serializable
    data class SetState(
        val key: String,
        val value: String
    ) : UiAction()
    
    /**
     * Toggle boolean state value.
     * @param key State key to toggle
     */
    @Serializable
    data class ToggleState(
        val key: String
    ) : UiAction()
    
    /**
     * Show a snackbar message.
     * @param message Message text
     * @param duration Duration in milliseconds (optional)
     * @param actionLabel Optional action button label
     */
    @Serializable
    data class ShowSnackbar(
        val message: String,
        val duration: Long? = null,
        val actionLabel: String? = null
    ) : UiAction()
    
    /**
     * Show a dialog.
     * @param title Dialog title
     * @param message Dialog message
     * @param confirmLabel Confirm button label (default: "OK")
     * @param cancelLabel Optional cancel button label
     * @param onConfirm Optional action to execute on confirm
     * @param onCancel Optional action to execute on cancel
     */
    @Serializable
    data class ShowDialog(
        val title: String,
        val message: String,
        val confirmLabel: String = "OK",
        val cancelLabel: String? = null,
        val onConfirm: String? = null,
        val onCancel: String? = null
    ) : UiAction()
    
    /**
     * Refresh current screen data.
     */
    @Serializable
    data object Refresh : UiAction()
    
    /**
     * Open native share dialog.
     * @param text Text to share
     * @param url Optional URL to share
     */
    @Serializable
    data class Share(
        val text: String,
        val url: String? = null
    ) : UiAction()
    
    /**
     * Execute multiple actions sequentially.
     * @param actions List of actions to execute
     */
    @Serializable
    data class Batch(
        val actions: List<UiAction>
    ) : UiAction()
}
