package com.itsvks.code.input

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import com.itsvks.code.CodeEditorState
import com.itsvks.code.core.TextPosition
import com.itsvks.code.core.TextPositionRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EditorInputConnection(
    private val state: CodeEditorState,
    private val sendKeyEventHandler: suspend (androidx.compose.ui.input.key.KeyEvent) -> Boolean
) : InputConnection {

    private val TAG = "EditorInputConnection"
    private var composingRegion: TextPositionRange? = null

    private val scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())

    override fun beginBatchEdit(): Boolean = true
    override fun endBatchEdit(): Boolean = true

    override fun clearMetaKeyStates(states: Int): Boolean = true
    override fun closeConnection() {}

    override fun commitCompletion(text: CompletionInfo?): Boolean = false
    override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean = false
    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean = false

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        if (text == null) return false

        Log.d(TAG, "commitText: text=$text, newCursorPosition=$newCursorPosition")

        // Handle newline (Enter) specially
        //
        // Some keyboards (like GBoard) don't send key events for
        // keys like Backspace or Enter and instead only
        // use deleteSurroundingText() or commitText().
        // Other keyboards (like physical or AOSP keyboard) do
        // send sendKeyEvent(), especially for keys like Backspace or Enter.
        if (text == "\n") {
            val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
            scope.launch { sendKeyEventHandler(androidx.compose.ui.input.key.KeyEvent(event)) }
            return true
        }

        // Delete selected text before inserting new one
        if (state.selectionRange != null) {
            state.buffer.deleteRange(state.selectionRange!!)
            state.clearSelection()
        }

        state.insertText(text.toString())
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        Log.d(TAG, "deleteSurroundingText: before=$beforeLength, after=$afterLength")

        if (state.isTextSelected()) {
            state.buffer.deleteRange(state.selectionRange!!)
            state.moveCursorTo(state.selectionRange!!.start)
            state.clearSelection()
            return true
        }

        if (beforeLength == 1 && afterLength == 0) {
            val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
            scope.launch { sendKeyEventHandler(androidx.compose.ui.input.key.KeyEvent(event)) }
            return true
        }

        val cursor = state.cursorPosition
        val cursorIndex = positionToIndex(cursor)

        val startIndex = (cursorIndex - beforeLength).coerceAtLeast(0)
        val endIndex = (cursorIndex + afterLength).coerceAtMost(state.buffer.length)

        if (startIndex >= endIndex) return false

        state.buffer.deleteRange(
            indexToPosition(startIndex),
            indexToPosition(endIndex)
        )
        state.moveCursorTo(indexToPosition(startIndex))
        return true
    }

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        return deleteSurroundingText(beforeLength, afterLength)
    }

    override fun finishComposingText(): Boolean {
        composingRegion = null
        return true
    }

    override fun getCursorCapsMode(reqModes: Int): Int = 0

    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText {
        val text = state.buffer.text
        val cursorIndex = positionToIndex(state.cursorPosition)

        return ExtractedText().apply {
            this.text = text
            this.partialStartOffset = 0
            this.partialEndOffset = text.length
            this.flags = 0
            this.selectionStart = cursorIndex
            this.selectionEnd = cursorIndex

            state.selectionRange?.let {
                this.selectionStart = positionToIndex(it.start)
                this.selectionEnd = positionToIndex(it.end)
            }
        }
    }

    override fun getHandler(): Handler? = null

    override fun getSelectedText(flags: Int): CharSequence {
        val selection = state.selectionRange ?: return ""
        return state.buffer.slice(selection)
    }

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence? {
        val cursorIndex = positionToIndex(state.cursorPosition)
        val end = (cursorIndex + n).coerceAtMost(state.buffer.length)
        return state.buffer.slice(indexToPosition(cursorIndex), indexToPosition(end))
    }

    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
        val cursorIndex = positionToIndex(state.cursorPosition)
        val start = (cursorIndex - n).coerceAtLeast(0)
        return state.buffer.slice(indexToPosition(start), indexToPosition(cursorIndex))
    }

    override fun performContextMenuAction(id: Int): Boolean = false
    override fun performEditorAction(editorAction: Int): Boolean = false
    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean = false
    override fun reportFullscreenMode(enabled: Boolean): Boolean = false
    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean = false

    override fun sendKeyEvent(event: KeyEvent?): Boolean {
        Log.d(TAG, "sendKeyEvent: event=$event")
        return event?.let { runBlocking { sendKeyEventHandler(androidx.compose.ui.input.key.KeyEvent(it)) } } ?: false
    }

    override fun setComposingRegion(start: Int, end: Int): Boolean {
        composingRegion = TextPositionRange(indexToPosition(start), indexToPosition(end))
        return true
    }

    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
        if (text == null) return false

        Log.d(TAG, "setComposingText: text=$text")

        if (composingRegion != null) {
            state.buffer.replace(composingRegion!!, text.toString())
            val newPos = state.buffer.advancePosition(composingRegion!!.start, text.toString())
            state.moveCursorTo(newPos)
            composingRegion = TextPositionRange(composingRegion!!.start, newPos)
        } else {
            if (state.selectionRange != null) {
                state.buffer.deleteRange(state.selectionRange!!)
                state.clearSelection()
            }

            val currentPos = state.cursorPosition
            state.buffer.insertText(currentPos, text.toString())
            val newPos = state.buffer.advancePosition(currentPos, text.toString())
            state.moveCursorTo(newPos)
            composingRegion = TextPositionRange(currentPos, newPos)
        }

        return true
    }

    override fun setSelection(start: Int, end: Int): Boolean {
        Log.d(TAG, "setSelection: start=$start, end=$end")

        // GBoard Issue

        // val startPos = indexToPosition(start)
        // val endPos = indexToPosition(end)

        // if (startPos == endPos) {
        //     state.clearSelection()
        //     state.moveCursorTo(startPos)
        // } else {
        //     state.selectRange(startPos..endPos)
        //     state.moveCursorTo(endPos)
        // }

        return true
    }

    private fun positionToIndex(position: TextPosition): Int {
        val lineStart = state.buffer.lineToChar(position.line)
        return lineStart + position.column
    }

    private fun indexToPosition(index: Int): TextPosition {
        return state.buffer.indexToPosition(index)
    }
}
