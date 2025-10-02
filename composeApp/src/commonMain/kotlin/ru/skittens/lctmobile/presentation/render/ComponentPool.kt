package ru.skittens.lctmobile.presentation.render

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class ComponentPool {
    private val textStates = ArrayDeque<MutableState<String>>()
    private val booleanStates = ArrayDeque<MutableState<Boolean>>()

    fun borrowTextState(initial: String = ""): MutableState<String> =
        synchronized(textStates) { textStates.removeFirstOrNull() }?.also { it.value = initial }
            ?: mutableStateOf(initial)

    fun recycleTextState(state: MutableState<String>) {
        synchronized(textStates) {
            textStates.add(state)
        }
    }

    fun borrowBooleanState(initial: Boolean = false): MutableState<Boolean> =
        synchronized(booleanStates) { booleanStates.removeFirstOrNull() }?.also { it.value = initial }
            ?: mutableStateOf(initial)

    fun recycleBooleanState(state: MutableState<Boolean>) {
        synchronized(booleanStates) {
            booleanStates.add(state)
        }
    }
}
