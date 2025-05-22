package com.itsvks.code.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.PlatformTextInputModifierNode
import androidx.compose.ui.platform.PlatformTextInputSession
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.establishTextInputSession
import com.itsvks.code.CodeEditorState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class CodeEditorTextInputNode(
    var state: CodeEditorState,
    var softwareKeyboardController: SoftwareKeyboardController?,
    var sendKeyEventHandler: suspend (KeyEvent) -> Boolean
) : Modifier.Node(), FocusEventModifierNode, PlatformTextInputModifierNode {
    private var focusedJob: Job? = null

    override fun onFocusEvent(focusState: FocusState) {
        println("Focus: $focusState")

        focusedJob?.cancel()
        focusedJob = if (focusState.isFocused) {
            softwareKeyboardController?.show()
            coroutineScope.launch {
                establishTextInputSession {
                    launch {
                        // TODO: Observe text field state, call into system to update it as
                        //  required by the platform.
                    }

                    val request: PlatformTextInputMethodRequest = createInputRequest()
                    startInputMethod(request)
                }
            }
        } else {
            softwareKeyboardController?.hide()
            null
        }
    }

    private fun PlatformTextInputSession.createInputRequest(): PlatformTextInputMethodRequest {
        return object : PlatformTextInputMethodRequest {
            override fun createInputConnection(outAttributes: EditorInfo): InputConnection {
                val connection = EditorInputConnection(
                    view = view,
                    state = state,
                    sendKeyEventHandler = sendKeyEventHandler
                )
                outAttributes.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                outAttributes.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                return connection
            }
        }
    }
}

internal data class CodeEditorTextInputElement(
    private val state: CodeEditorState,
    private val keyboardController: SoftwareKeyboardController?,
    private val sendKeyEventHandler: suspend (KeyEvent) -> Boolean
) : ModifierNodeElement<CodeEditorTextInputNode>() {

    override fun InspectorInfo.inspectableProperties() {
        name = "codeEditorTextInput"
        properties["state"] = state
        properties["keyboardController"] = keyboardController
        properties["sendKeyEventHandler"] = sendKeyEventHandler
    }

    override fun create(): CodeEditorTextInputNode {
        return CodeEditorTextInputNode(state, keyboardController, sendKeyEventHandler)
    }

    override fun update(node: CodeEditorTextInputNode) {
        node.state = state
        node.softwareKeyboardController = keyboardController
        node.sendKeyEventHandler = sendKeyEventHandler
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CodeEditorTextInputElement

        if (state != other.state) return false
        if (keyboardController != other.keyboardController) return false
        if (sendKeyEventHandler != other.sendKeyEventHandler) return false

        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + (keyboardController?.hashCode() ?: 0)
        result = 31 * result + sendKeyEventHandler.hashCode()
        return result
    }
}

fun Modifier.codeEditorTextInput(
    state: CodeEditorState,
    keyboardController: SoftwareKeyboardController? = null,
    onKeyEvent: suspend (KeyEvent) -> Boolean = { false }
): Modifier = this then CodeEditorTextInputElement(state, keyboardController, onKeyEvent)
