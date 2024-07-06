package com.chess.chessappnimcovichsistem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp



class Beta {
}
@Preview
@Composable
fun Chessboard(){
    LazyVerticalGrid(columns = GridCells.Fixed(8)) {
        items(64) { index ->
            // Здесь вы определяете содержимое каждой ячейки сетки,
            // например, отображаете изображение или текст.
            Box(modifier = Modifier.size(48.dp).background(Color.Blue)) {
                Text("Ячейка ${64 - index}")
            }
        }
    }
}

