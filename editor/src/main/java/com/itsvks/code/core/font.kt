package com.itsvks.code.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun rememberJetBrainsMonoFontFamily(): FontFamily {
    val context = LocalContext.current

    return remember {
        val assetManager = context.assets
        val fonts = listOf(
            Triple("fonts/JetBrainsMono-Thin.ttf", FontWeight.Thin, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-ThinItalic.ttf", FontWeight.Thin, FontStyle.Italic),
            Triple("fonts/JetBrainsMono-ExtraLight.ttf", FontWeight.ExtraLight, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-ExtraLightItalic.ttf", FontWeight.ExtraLight, FontStyle.Italic),
            Triple("fonts/JetBrainsMono-Light.ttf", FontWeight.Light, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-LightItalic.ttf", FontWeight.Light, FontStyle.Italic),
            Triple("fonts/JetBrainsMono-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-Italic.ttf", FontWeight.Normal, FontStyle.Italic),
            Triple("fonts/JetBrainsMono-Medium.ttf", FontWeight.Medium, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-MediumItalic.ttf", FontWeight.Medium, FontStyle.Italic),
            Triple("fonts/JetBrainsMono-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-SemiBoldItalic.ttf", FontWeight.SemiBold, FontStyle.Italic),
            Triple("fonts/JetBrainsMono-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-BoldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
            Triple("fonts/JetBrainsMono-ExtraBold.ttf", FontWeight.ExtraBold, FontStyle.Normal),
            Triple("fonts/JetBrainsMono-ExtraBoldItalic.ttf", FontWeight.ExtraBold, FontStyle.Italic)
        )

        val loadedFonts = fonts.mapNotNull { (path, weight, style) ->
            try {
                Font(path, assetManager = assetManager, weight = weight, style = style)
            } catch (e: Exception) {
                null
            }
        }

        if (loadedFonts.isNotEmpty()) {
            FontFamily(loadedFonts)
        } else {
            // fallback to default monospace
            FontFamily.Monospace
        }
    }
}
