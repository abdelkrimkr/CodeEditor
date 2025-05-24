package com.itsvks.code.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.ScrollbarOverlay(listState: LazyListState) {
    val scrollbarMetrics by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val visibleItems = listState.layoutInfo.visibleItemsInfo.size
            val firstVisibleItemIndex = listState.firstVisibleItemIndex

            if (totalItems == 0 || totalItems == visibleItems) {
                // No scrollbar needed
                null
            } else {
                val scrollFraction = firstVisibleItemIndex / (totalItems - visibleItems).toFloat()
                val scrollbarHeightFraction = visibleItems / totalItems.toFloat()
                Pair(scrollFraction, scrollbarHeightFraction)
            }
        }
    }

    if (scrollbarMetrics != null) {
        val (scrollFraction, scrollbarHeightFraction) = scrollbarMetrics!!
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp)
        ) {
            val canvasHeight = size.height
            val scrollbarHeight = canvasHeight * scrollbarHeightFraction
            val scrollbarOffsetY = canvasHeight * scrollFraction
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(0f, scrollbarOffsetY),
                size = Size(size.width, scrollbarHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
