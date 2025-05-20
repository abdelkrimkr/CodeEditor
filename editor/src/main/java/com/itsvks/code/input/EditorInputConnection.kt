package com.itsvks.code.input

import android.view.View
import android.view.inputmethod.BaseInputConnection
import com.itsvks.code.CodeEditorState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class EditorInputConnection(
    private val view: View,
    private val state: CodeEditorState,
    private val sendKeyEventHandler: suspend (androidx.compose.ui.input.key.KeyEvent) -> Boolean
) : BaseInputConnection(view, true) {

    private val TAG = "EditorInputConnection"
    private val scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
}
