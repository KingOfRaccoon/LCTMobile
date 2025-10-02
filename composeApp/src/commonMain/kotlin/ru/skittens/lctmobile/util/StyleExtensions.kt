package ru.skittens.lctmobile.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

@Stable
fun Modifier.applyStyle(style: JsonObject?): Modifier {
    if (style == null) return this
    var modifier = this

    // Padding
    val padding = style.dpValue("padding")
    val paddingHorizontal = style.dpValue("paddingHorizontal")
    val paddingVertical = style.dpValue("paddingVertical")
    modifier = when {
        padding != null -> modifier.padding(padding)
        paddingHorizontal != null || paddingVertical != null -> modifier.padding(
            horizontal = paddingHorizontal ?: 0.dp,
            vertical = paddingVertical ?: 0.dp
        )
        else -> modifier
    }
    
    // Margin (using padding as margin)
    val margin = style.dpValue("margin")
    val marginHorizontal = style.dpValue("marginHorizontal")
    val marginVertical = style.dpValue("marginVertical")
    modifier = when {
        margin != null -> modifier.padding(margin)
        marginHorizontal != null || marginVertical != null -> modifier.padding(
            horizontal = marginHorizontal ?: 0.dp,
            vertical = marginVertical ?: 0.dp
        )
        else -> modifier
    }

    // Size
    val widthValue = style.stringValue("width")
    modifier = when (widthValue) {
        "fill", "match_parent", "100%" -> modifier.fillMaxWidth()
        else -> style.dpValue("width")?.let { modifier.width(it) } ?: modifier
    }
    
    val heightValue = style.stringValue("height")
    modifier = when (heightValue) {
        "fill", "match_parent", "100%" -> modifier.fillMaxHeight()
        else -> style.dpValue("height")?.let { modifier.height(it) } ?: modifier
    }

    // Border radius (rounded corners)
    val borderRadius = style.dpValue("borderRadius")
    val shape = if (borderRadius != null) {
        RoundedCornerShape(borderRadius)
    } else null
    
    if (shape != null) {
        modifier = modifier.clip(shape)
    }
    
    // Background color
    style.stringValue("backgroundColor")?.let { colorString ->
        parseColor(colorString)?.let { 
            modifier = if (shape != null) {
                modifier.background(it, shape)
            } else {
                modifier.background(it)
            }
        }
    }
    
    // Border
    val borderWidth = style.dpValue("borderWidth")
    val borderColor = style.stringValue("borderColor")?.let(::parseColor)
    if (borderWidth != null && borderColor != null) {
        modifier = if (shape != null) {
            modifier.border(borderWidth, borderColor, shape)
        } else {
            modifier.border(borderWidth, borderColor)
        }
    }
    
    // Shadow/Elevation
    val elevation = style.dpValue("elevation")
    if (elevation != null && elevation > 0.dp) {
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape ?: RoundedCornerShape(0.dp)
        )
    }

    return modifier
}

fun parseColor(value: String): Color? = runCatching {
    if (value.startsWith("#")) {
        val hex = value.removePrefix("#")
        val colorLong = hex.toLong(16)
        when (hex.length) {
            6 -> Color(colorLong or 0xFF000000)
            8 -> Color(colorLong)
            else -> null
        }
    } else null
}.getOrNull()

fun JsonObject.dpValue(key: String): Dp? = (this[key] as? JsonPrimitive)
    ?.contentOrNull
    ?.toFloatOrNull()
    ?.dp

fun JsonObject.booleanValue(key: String): Boolean? = (this[key] as? JsonPrimitive)?.booleanOrNull

fun JsonObject.stringValue(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

fun JsonObject.floatValue(key: String): Float? = (this[key] as? JsonPrimitive)
    ?.contentOrNull
    ?.toFloatOrNull()

