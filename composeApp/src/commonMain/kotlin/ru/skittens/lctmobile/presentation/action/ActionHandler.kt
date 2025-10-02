package ru.skittens.lctmobile.presentation.action

import ru.skittens.shared.action.UiAction

/**
 * Interface for handling UI actions.
 * Platform-specific implementations should provide actual functionality.
 */
interface ActionHandler {
    
    /**
     * Execute a UI action.
     * @param action Action to execute
     * @param onComplete Callback invoked when action completes (success or failure)
     */
    suspend fun handle(action: UiAction, onComplete: (Result<Unit>) -> Unit = {})
    
    /**
     * Check if this handler can handle the given action type.
     */
    fun canHandle(action: UiAction): Boolean = true
}

/**
 * Default action handler implementation.
 * Provides basic handling for common actions.
 */
class DefaultActionHandler(
    private val onNavigate: (String, Boolean) -> Unit = { _, _ -> },
    private val onNavigateBack: () -> Unit = {},
    private val onShowSnackbar: (String, String?) -> Unit = { _, _ -> },
    private val onShowDialog: (String, String, String, String?, () -> Unit, () -> Unit) -> Unit = { _, _, _, _, _, _ -> },
    private val onRefresh: () -> Unit = {},
    private val onApiCall: suspend (String, String, String?) -> Result<Unit> = { _, _, _ -> Result.success(Unit) }
) : ActionHandler {
    
    private val stateMap = mutableMapOf<String, String>()
    
    override suspend fun handle(action: UiAction, onComplete: (Result<Unit>) -> Unit) {
        try {
            when (action) {
                is UiAction.Navigate -> {
                    onNavigate(action.screenId, action.clearStack)
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.NavigateBack -> {
                    onNavigateBack()
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.NavigateExternal -> {
                    // Platform-specific handling required
                    println("Navigate to external URL: ${action.url}")
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.ApiCall -> {
                    val bodyString = action.body?.toString()
                    val result = onApiCall(action.endpoint, action.method, bodyString)
                    onComplete(result)
                }
                
                is UiAction.SetState -> {
                    stateMap[action.key] = action.value
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.ToggleState -> {
                    val currentValue = stateMap[action.key]?.toBooleanStrictOrNull() ?: false
                    stateMap[action.key] = (!currentValue).toString()
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.ShowSnackbar -> {
                    onShowSnackbar(action.message, action.actionLabel)
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.ShowDialog -> {
                    onShowDialog(
                        action.title,
                        action.message,
                        action.confirmLabel,
                        action.cancelLabel,
                        {}, // onConfirm callback
                        {}  // onCancel callback
                    )
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.Refresh -> {
                    onRefresh()
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.Share -> {
                    // Platform-specific handling required
                    println("Share: ${action.text}, url: ${action.url}")
                    onComplete(Result.success(Unit))
                }
                
                is UiAction.Batch -> {
                    var hasError = false
                    for (batchAction in action.actions) {
                        if (hasError) break
                        handle(batchAction) { result ->
                            if (result.isFailure) {
                                hasError = true
                                onComplete(result)
                            }
                        }
                    }
                    if (!hasError) {
                        onComplete(Result.success(Unit))
                    }
                }
            }
        } catch (e: Exception) {
            onComplete(Result.failure(e))
        }
    }
    
    /**
     * Get current state value by key.
     */
    fun getState(key: String): String? = stateMap[key]
    
    /**
     * Get all current state.
     */
    fun getAllState(): Map<String, String> = stateMap.toMap()
}
