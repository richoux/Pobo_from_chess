package fr.richoux.pobo.ui

import androidx.compose.ui.graphics.Color

val teal200 = Color(0xFF03DAC5)
val blue800 = Color(0xFF1565c0)
val blueVariant = Color(0xFF003b8e)
val blue300 = Color(0xFF64b5f6)
val lightGreen = Color(0xFFeafce3)

object BoardColors {
    val lastMoveLight = lightGreen.copy(alpha = 0.3f)
    val lastMoveDark = blue800.copy(alpha = 0.3f)
    val selected = Color.Red
    val lightSquare = Color(0xFFebefff)
    val darkSquare = Color(0xFFcce0ff)
//    val lastMoveColor = blueVariant.copy(alpha = 0.5f)
//    val lightSquare = Color(0xFFF0D9B5)
//    val darkSquare = Color(0xFF946f51)
}