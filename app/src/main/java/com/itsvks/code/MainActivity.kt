package com.itsvks.code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itsvks.code.component.CodeEditor
import com.itsvks.code.language.KotlinLanguage
import com.itsvks.code.language.RustLanguage
import com.itsvks.code.theme.AtomOneDarkTheme
import com.itsvks.code.ui.theme.CodeEditorTheme
import kotlinx.coroutines.delay
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CodeEditorTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) { innerPadding ->
                    LaunchedEffect(Unit) {
                        assets.open("sample.rs").bufferedReader().use {
                            File(filesDir, "sample.rs").writeText(it.readText())
                        }
                    }

                    val editorState = rememberCodeEditorState(
                        filePath = File(filesDir, "sample.rs").absolutePath,
                        language = RustLanguage,
                        theme = AtomOneDarkTheme
                    )

                    LaunchedEffect(Unit) {
                        delay(3000)
                        val rust = assets.open("sample.rs").bufferedReader().use { it.readText() }
//                        editorState.setText(rust)
//                        editorState.language = RustLanguage
                    }

                    CodeEditor(
                        state = editorState,
                        cursorWidth = 2f,
                        blinkCursor = true,
                        showInvisibleCharacters = true,
                        modifier = Modifier
                            .padding(innerPadding)
                            .imePadding()
                            //.height(500.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CodeEditorTheme {
        Greeting("Android")
    }
}
