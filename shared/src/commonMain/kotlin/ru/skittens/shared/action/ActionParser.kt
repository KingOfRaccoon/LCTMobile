package ru.skittens.shared.action

import kotlinx.serialization.json.JsonObject
import ru.skittens.shared.util.SharedJson

/**
 * Parses JSON action definitions into UiAction objects.
 * 
 * Expected JSON format:
 * ```json
 * {
 *   "type": "navigate",
 *   "screenId": "screen_123",
 *   "clearStack": false
 * }
 * ```
 */
object ActionParser {
    
    /**
     * Parse a JSON object into a UiAction.
     * Returns null if parsing fails or type is unknown.
     */
    fun parse(json: JsonObject?): UiAction? {
        if (json == null) return null
        
        return try {
            val type = json["type"]?.toString()?.trim('"') ?: return null
            
            when (type.lowercase()) {
                "navigate" -> parseNavigate(json)
                "navigate_back", "navigateback", "back" -> UiAction.NavigateBack
                "navigate_external", "navigateexternal", "open_url" -> parseNavigateExternal(json)
                "api_call", "apicall", "fetch" -> parseApiCall(json)
                "set_state", "setstate", "update" -> parseSetState(json)
                "toggle_state", "togglestate", "toggle" -> parseToggleState(json)
                "show_snackbar", "showsnackbar", "snackbar" -> parseShowSnackbar(json)
                "show_dialog", "showdialog", "dialog" -> parseShowDialog(json)
                "refresh", "reload" -> UiAction.Refresh
                "share" -> parseShare(json)
                "batch", "sequence" -> parseBatch(json)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseNavigate(json: JsonObject): UiAction.Navigate? {
        val screenId = json["screenId"]?.toString()?.trim('"')
            ?: json["target"]?.toString()?.trim('"')
            ?: return null
        val clearStack = json["clearStack"]?.toString()?.toBoolean() ?: false
        return UiAction.Navigate(screenId, clearStack)
    }
    
    private fun parseNavigateExternal(json: JsonObject): UiAction.NavigateExternal? {
        val url = json["url"]?.toString()?.trim('"') ?: return null
        return UiAction.NavigateExternal(url)
    }
    
    private fun parseApiCall(json: JsonObject): UiAction.ApiCall? {
        val endpoint = json["endpoint"]?.toString()?.trim('"') ?: return null
        val method = json["method"]?.toString()?.trim('"')?.uppercase() ?: "GET"
        val body = json["body"] as? JsonObject
        val onSuccess = json["onSuccess"]?.toString()?.trim('"')
        val onError = json["onError"]?.toString()?.trim('"')
        return UiAction.ApiCall(endpoint, method, body, onSuccess, onError)
    }
    
    private fun parseSetState(json: JsonObject): UiAction.SetState? {
        val key = json["key"]?.toString()?.trim('"') ?: return null
        val value = json["value"]?.toString()?.trim('"') ?: ""
        return UiAction.SetState(key, value)
    }
    
    private fun parseToggleState(json: JsonObject): UiAction.ToggleState? {
        val key = json["key"]?.toString()?.trim('"') ?: return null
        return UiAction.ToggleState(key)
    }
    
    private fun parseShowSnackbar(json: JsonObject): UiAction.ShowSnackbar? {
        val message = json["message"]?.toString()?.trim('"') ?: return null
        val duration = json["duration"]?.toString()?.toLongOrNull()
        val actionLabel = json["actionLabel"]?.toString()?.trim('"')
        return UiAction.ShowSnackbar(message, duration, actionLabel)
    }
    
    private fun parseShowDialog(json: JsonObject): UiAction.ShowDialog? {
        val title = json["title"]?.toString()?.trim('"') ?: ""
        val message = json["message"]?.toString()?.trim('"') ?: return null
        val confirmLabel = json["confirmLabel"]?.toString()?.trim('"') ?: "OK"
        val cancelLabel = json["cancelLabel"]?.toString()?.trim('"')
        val onConfirm = json["onConfirm"]?.toString()?.trim('"')
        val onCancel = json["onCancel"]?.toString()?.trim('"')
        return UiAction.ShowDialog(title, message, confirmLabel, cancelLabel, onConfirm, onCancel)
    }
    
    private fun parseShare(json: JsonObject): UiAction.Share? {
        val text = json["text"]?.toString()?.trim('"') ?: return null
        val url = json["url"]?.toString()?.trim('"')
        return UiAction.Share(text, url)
    }
    
    private fun parseBatch(json: JsonObject): UiAction.Batch? {
        val actionsArray = json["actions"] as? List<*> ?: return null
        val actions = actionsArray.mapNotNull { 
            (it as? JsonObject)?.let { parse(it) }
        }
        return if (actions.isNotEmpty()) UiAction.Batch(actions) else null
    }
}
