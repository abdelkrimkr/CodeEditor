package com.itsvks.code.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsvks.code.component.CodeEditor
import com.itsvks.code.language.JavaLanguage
import com.itsvks.code.theme.AtomOneDarkTheme
import com.itsvks.code.example.ui.theme.CodeEditorTheme
import com.itsvks.code.language.KotlinLanguage
import com.itsvks.code.rememberCodeEditorState
import com.itsvks.code.theme.AtomOneLightTheme
import com.itsvks.code.theme.DraculaTheme
import com.itsvks.code.theme.JetBrainsDarkTheme
import com.itsvks.code.theme.VsCodeDarkTheme
import kotlinx.coroutines.delay

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
                    val editorState = rememberCodeEditorState(
                        assets.open("View.java"),
                        language = JavaLanguage,
                        theme = VsCodeDarkTheme
                    )

                    LaunchedEffect(Unit) {
                        showToast("Changing file in 3 seconds...")

                        delay(3000)
                        editorState.setInputStream(assets.open("temp.kt"))
                        editorState.language = KotlinLanguage
                        editorState.theme = JetBrainsDarkTheme
                    }

                    CodeEditor(
                        state = editorState,
                        softWrap = true,
                        initialFontSize = 15.sp,
                        editable = true,
                        enableZoomGestures = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding()
                    )
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
