package com.itsvks.code.core

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

@Composable
fun rememberJetBrainsMonoFontFamily(): FontFamily {
    val context = LocalContext.current
    val typeface = remember {
        try {
            Typeface.createFromAsset(context.assets, "JetBrainsMono-Regular.ttf")
        } catch (e: Exception) {
            Typeface.MONOSPACE
        }
    }

    return remember(typeface) { FontFamily(typeface) }
}
