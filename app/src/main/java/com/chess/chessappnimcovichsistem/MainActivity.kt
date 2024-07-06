package com.chess.chessappnimcovichsistem


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessboardScreen()
        }
    }
}

@Preview
@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessboardScreen() {
    val boardState = remember { mutableStateOf(initialBoardStateMutable()) }
    val moveHistory = remember { mutableStateListOf<String>() }
    val gameHistory = remember { mutableStateListOf<List<Move>>() }
    var currentGameIndex by remember { mutableStateOf<Int?>(null) }
    var currentMoveIndex by remember { mutableStateOf(0) }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }

    Column {
        Chessboard(boardState, moveHistory)
        MoveHistory(moveHistory)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                if (currentGameIndex != null && currentMoveIndex > 0) {
                    currentMoveIndex--
                    applyMoveToBoard(boardState, gameHistory[currentGameIndex!!], currentMoveIndex)
                }
            }) {
                Text("Previous Move")
            }
            Button(onClick = {
                if (currentGameIndex != null && currentMoveIndex < gameHistory[currentGameIndex!!].size - 1) {
                    currentMoveIndex++
                    applyMoveToBoard(boardState, gameHistory[currentGameIndex!!], currentMoveIndex)
                }
            }) {
                Text("Next Move")
            }
        }
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter Game Notation (e.g., e2e4)") }
        )
        Button(onClick = {
            val moves = mutableListOf<Move>()
            var index = 0
            while (index < inputText.text.length) {
                val moveString =
                    inputText.text.substring(index, minOf(index + 5, inputText.text.length)).trim()
                if (moveString.length == 5) { // Check for valid move length
                    val fromCol = moveString[1].lowercaseChar().code - 'a'.code
                    val fromRow = 8 - moveString[2].digitToInt()
                    val toCol = moveString[3].lowercaseChar().code - 'a'.code
                    val toRow = 8 - moveString[4].digitToInt()
                    moves.add(Move(fromRow, fromCol, toRow, toCol))
                }
                index += 5 // Move to the next potential move
            }
            gameHistory.add(moves)
            inputText = TextFieldValue("")
        }) {
            Text("Save Game")
        }
        GameList(gameHistory) { index ->
            currentGameIndex = index
            currentMoveIndex = 0 // Reset move index when selecting a new game
            applyMoveToBoard(boardState, gameHistory[index], currentMoveIndex)
        }
    }
}

@Composable
fun Chessboard(
    boardState: MutableState<MutableList<MutableList<Piece?>>>,
    moveHistory: SnapshotStateList<String>
) {
    var selectedPiece by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val darkGoldenrod = Color(0xFFB8860B)

    LazyVerticalGrid(columns = GridCells.Fixed(8)) {
        items(64) { index ->
            val reversedIndex = 63 - index
            val row = index / 8
            val col = index % 8
            val piece = boardState.value[row][col]
            val isLightSquare = (row + col) % 2 == 0

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isLightSquare) {
                            if (selectedPiece == Pair(row, col)) darkGoldenrod else Color.LightGray
                        } else {
                            if (selectedPiece == Pair(row, col)) darkGoldenrod else Color.DarkGray
                        }
                    )
                    .clickable {
                        if (selectedPiece == null) {
                            if (piece != null) {
                                selectedPiece = Pair(row, col)
                            }
                        } else {
                            val (fromRow, fromCol) = selectedPiece!!
                            if (isValidMove(boardState.value, fromRow, fromCol, row, col)) {
                                val movingPiece = boardState.value[fromRow][fromCol]
                                val moveNotation =
                                    getMoveNotation(fromRow, fromCol, row, col, movingPiece)
                                moveHistory.add(moveNotation)

                                val newBoardState = boardState.value
                                    .map { it.toMutableList() }
                                    .toMutableList()
                                newBoardState[row][col] = newBoardState[fromRow][fromCol]
                                newBoardState[fromRow][fromCol] = null
                                boardState.value = newBoardState
                            }
                            selectedPiece = null
                        }
                    }
            ) {
                // Отображаем координаты
                if (col == 7) { // Крайний правый столбец
                    Text(
                        text = (8 - row).toString(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp), // Верхний правый угол
                        fontSize = 8.sp,
                        color = if (isLightSquare) Color.Black else Color.White
                    )
                }
                if (row == 7) { // Нижний ряд
                    Text(
                        text = ('A' + col).toString(),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(2.dp), // Нижний левый угол
                        fontSize = 8.sp,
                        color = if (isLightSquare) Color.Black else Color.White
                    )
                }

                if (piece != null) {
                    Image(
                        painter = painterResource(id = getPieceImageResource(piece)),
                        contentDescription = "Шахматная фигура",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MoveHistory(moveHistory: List<String>) {
    LazyColumn {
        items(moveHistory.size) { index ->
            Text(text = moveHistory[index])
        }
    }
}

@Composable
fun GameList(gameHistory: List<List<Move>>, onGameSelect: (Int) -> Unit) {
    LazyColumn {
        items(gameHistory.size) { index ->
            Button(onClick = { onGameSelect(index) }) {
                Text("Game ${index + 1}")
            }
        }
    }
}

fun applyMoveToBoard(
    boardState: MutableState<MutableList<MutableList<Piece?>>>,
    moves: List<Move>,
    moveIndex: Int
) {
    boardState.value = initialBoardStateMutable() // Сбрасываем доску к начальному состоянию
    for(i in 0 until moveIndex) {
        val move = moves[i]
        val newBoardState = boardState.value.map { it.toMutableList() }.toMutableList()
        newBoardState[move.toRow][move.toCol] = newBoardState[move.fromRow][move.fromCol]
        newBoardState[move.fromRow][move.fromCol] = null
        boardState.value = newBoardState
    }
}

// Функция для получения нотации хода
fun getMoveNotation(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, piece: Piece?): String {
    val fromSquare = ('a' + fromCol) + (8 - fromRow).toString()
    val toSquare = ('a' + toCol) + (8 - toRow).toString()
    val pieceSymbol = when (piece?.type) {
        PieceType.KING -> "K"
        PieceType.QUEEN -> "Q"
        PieceType.ROOK -> "R"
        PieceType.BISHOP -> "B"
        PieceType.KNIGHT -> "N"
        else -> "" // Для пешек символ не указывается
    }
    return "$pieceSymbol$fromSquare$toSquare"
}

// Класс Piece для представления шахматных фигур
data class Piece(val type: PieceType, val color: PieceColor)

// Перечисление типов фигур
enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

// Перечисление цветов фигур
enum class PieceColor {
    WHITE, BLACK
}

// Функция для проверки корректности хода (пока разрешены все ходы)
fun isValidMove(
    board: MutableList<MutableList<Piece?>>,
    fromRow: Int,
    fromCol: Int,
    toRow: Int,
    toCol: Int
): Boolean {
    // Разрешаем все ходы
    return true
}

// Функция для инициализации начального состояния шахматной доски
fun initialBoardStateMutable(): MutableList<MutableList<Piece?>> {
    val board = MutableList(8) { MutableList<Piece?>(8) { null } }

    // Белые фигуры
    board[7][0] = Piece(PieceType.ROOK, PieceColor.WHITE)
    board[7][1] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
    board[7][2] = Piece(PieceType.BISHOP, PieceColor.WHITE)
    board[7][3] = Piece(PieceType.QUEEN, PieceColor.WHITE)
    board[7][4] = Piece(PieceType.KING, PieceColor.WHITE)
    board[7][5] = Piece(PieceType.BISHOP, PieceColor.WHITE)
    board[7][6] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
    board[7][7] = Piece(PieceType.ROOK, PieceColor.WHITE)

    board[6][0] = Piece(PieceType.PAWN, PieceColor.WHITE)
    board[6][1] = Piece(PieceType.PAWN, PieceColor.WHITE)
    board[6][2] = Piece(PieceType.PAWN, PieceColor.WHITE)
    board[6][3] = Piece(PieceType.PAWN, PieceColor.WHITE)
    board[6][4] = Piece(PieceType.PAWN, PieceColor.WHITE)
    board[6][5] = Piece(PieceType.PAWN, PieceColor.WHITE)
    board[6][6] = Piece(PieceType.PAWN, PieceColor.WHITE)
    board[6][7] = Piece(PieceType.PAWN, PieceColor.WHITE)

    // Черные фигуры
    board[0][0] = Piece(PieceType.ROOK, PieceColor.BLACK)
    board[0][1] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
    board[0][2] = Piece(PieceType.BISHOP, PieceColor.BLACK)
    board[0][3] = Piece(PieceType.QUEEN, PieceColor.BLACK)
    board[0][4] = Piece(PieceType.KING, PieceColor.BLACK)
    board[0][5] = Piece(PieceType.BISHOP, PieceColor.BLACK)
    board[0][6] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
    board[0][7] = Piece(PieceType.ROOK, PieceColor.BLACK)

    board[1][0] = Piece(PieceType.PAWN, PieceColor.BLACK)
    board[1][1] = Piece(PieceType.PAWN, PieceColor.BLACK)
    board[1][2] = Piece(PieceType.PAWN, PieceColor.BLACK)
    board[1][3] = Piece(PieceType.PAWN, PieceColor.BLACK)
    board[1][4] = Piece(PieceType.PAWN, PieceColor.BLACK)
    board[1][5] = Piece(PieceType.PAWN, PieceColor.BLACK)
    board[1][6] = Piece(PieceType.PAWN, PieceColor.BLACK)
    board[1][7] = Piece(PieceType.PAWN, PieceColor.BLACK)

    return board
}

// Функция для получения ресурса изображения в зависимости от типа и цвета фигуры
@Composable
fun getPieceImageResource(piece: Piece): Int {
    return when (piece.type) {
        PieceType.PAWN -> if (piece.color == PieceColor.WHITE) R.drawable.white_pawn else R.drawable.black_pawn
        PieceType.ROOK -> if (piece.color == PieceColor.WHITE) R.drawable.white_rook else R.drawable.black_rook
        PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) R.drawable.white_queen else R.drawable.black_queen
        PieceType.KING -> if (piece.color == PieceColor.WHITE) R.drawable.white_king else R.drawable.black_king
        PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) R.drawable.white_knight else R.drawable.black_knight
        PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) R.drawable.white_bishop else R.drawable.black_bishop
    }
}

data class Move(val fromRow: Int, val fromCol: Int, val toRow: Int, val toCol: Int)