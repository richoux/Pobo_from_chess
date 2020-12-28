package com.bentrengrove.chess

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.bentrengrove.chess.ui.BoardColors


@Composable
fun GameView(modifier: Modifier = Modifier, game: Game, selection: Position?, moves: List<Position>, didTap: (Position)->Unit) {
    Box(modifier) {
        val board = game.board

        // Highlight king in check, could potentially highlight other things here
        val dangerPositions = listOf(PieceColor.White, PieceColor.Black).mapNotNull { if (game.kingIsInCheck(it)) game.kingPosition(it) else null }

        BoardBackground(game.history.lastOrNull(), selection, dangerPositions, didTap)
        BoardLayout(pieces = board.allPieces, modifier = Modifier.fillMaxWidth().aspectRatio(1.0f))
        MovesView(board, moves)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MovesView(board: Board, moves: List<Position>) {
    Column {
        for (y in 0 until 8) {
            Row {
                for (x in 0 until 8) {
                    val position = Position(x, y)
                    Box(modifier = Modifier.weight(1f).aspectRatio(1.0f)) {
                        val piece = board.pieceAt(position)
                        val selected = moves.contains(position)
                        androidx.compose.animation.AnimatedVisibility(visible = selected, modifier = Modifier.matchParentSize(), enter = fadeIn(), exit = fadeOut()) {
                            val color = if (piece != null) BoardColors.attackColor else BoardColors.moveColor
                            Box(Modifier.clip(CircleShape).background(color).size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoardBackground(lastMove: Move?, selection: Position?, dangerPositions: List<Position>, didTap: (Position)->Unit) {
    Column {
        for (y in 0 until 8) {
            Row {
                for (x in 0 until 8) {
                    val position = Position(x, y)
                    val white = y % 2 == x % 2
                    val color = if (lastMove?.contains(position) == true || position == selection) {
                        BoardColors.lastMoveColor
                    } else if (dangerPositions.contains(position)) {
                        BoardColors.checkColor
                    } else {
                        if (white) BoardColors.lightSquare else BoardColors.darkSquare
                    }
                    Box(modifier = Modifier.weight(1f).background(color).aspectRatio(1.0f)
                            .clickable(
                                    onClick = { didTap(position) }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun PieceView(piece: Piece, modifier: Modifier = Modifier) {
    Image(imageResource(id = piece.imageResource()), modifier = modifier.padding(4.dp))
}

@Composable
private fun BoardLayout(
        modifier: Modifier = Modifier,
        pieces: List<Pair<Position, Piece>>) {
    val constraints = constraintsFor(pieces)
    ConstraintLayout(constraints, modifier) {
        pieces.forEach { (_, piece) ->
            PieceView(piece = piece, modifier = Modifier.layoutId(piece.id))
        }
    }
}

private fun constraintsFor(pieces: List<Pair<Position, Piece>>): ConstraintSet {
    return ConstraintSet {
        val horizontalGuidelines = (0..8).map { createGuidelineFromAbsoluteLeft(it.toFloat() / 8f) }
        val verticalGuidelines = (0..8).map { createGuidelineFromTop(it.toFloat() / 8f) }
        pieces.forEach { (position, piece) ->
            val pieceRef = createRefFor(piece.id)
            constrain(pieceRef) {
                top.linkTo(verticalGuidelines[position.y])
                bottom.linkTo(verticalGuidelines[position.y + 1])
                start.linkTo(horizontalGuidelines[position.x])
                end.linkTo((horizontalGuidelines[position.x + 1]))
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        }
    }
}