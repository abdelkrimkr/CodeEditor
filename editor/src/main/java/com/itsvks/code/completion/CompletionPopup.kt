package com.itsvks.code.completion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompletionPopup(
    items: List<String>,
    position: Offset,
    onItemSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .offset(position.x.dp, position.y.dp)
            .background(Color(0xFF252526), shape = RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Column {
            items.forEach { item ->
                Text(
                    text = item,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { onItemSelected(item) }
                )
            }
        }
    }
}
