package fr.richoux.pobo.engine

import android.util.Log
import androidx.annotation.DrawableRes
import fr.richoux.pobo.R

private const val TAG = "pobotag Board"
private var pieceCounter: Int = 0

sealed class PieceType(val value: Int) {
    object Po : PieceType(1) // small ones
    object Bo : PieceType(2) // big ones

    operator fun compareTo( other: PieceType ): Int {
        return this.value - other.value
    }
}

sealed class PieceColor {
    object Blue : PieceColor()
    object Red : PieceColor()

    fun other(): PieceColor {
        return if (this == Blue) Red else Blue
    }

    override fun toString(): String {
        return if (this == Blue) "Blue" else "Red"
    }
}

private fun pieceTypeFromId(id: String): Pair<PieceType, PieceColor> {
    val chars = id.toCharArray()
    val pieceColor = when (chars[0]) {
        'B' -> PieceColor.Blue
        'R' -> PieceColor.Red
        else -> throw IllegalStateException("First character should be B or R")
    }
    val pieceType = when (chars[1]) {
        'P' -> PieceType.Po
        'B' -> PieceType.Bo
        else -> throw IllegalStateException("Second character should be a piece type")
    }
    return pieceType to pieceColor
}

data class Piece(val id: String, val type: PieceType, val color: PieceColor) {
    companion object {
        fun pieceOrNullFromString(id: String?): Piece? {
            val id = id ?: return null
            val types = pieceTypeFromId(id)
            pieceCounter++
            val fullID: String = id + pieceCounter
            return Piece(fullID, types.first, types.second)
        }

        fun pieceFromString(id: String): Piece {
            val types = pieceTypeFromId(id)
            pieceCounter++
            val fullID: String = id + pieceCounter
            return Piece(fullID, types.first, types.second)
        }

        fun createPo(color: PieceColor): Piece {
            val id: String = when(color) {
                PieceColor.Blue -> "BP"
                PieceColor.Red -> "RP"
            }
            pieceCounter++
            val fullID: String = id + pieceCounter
            return Piece(fullID, PieceType.Po, color)
        }

        fun createBo(color: PieceColor): Piece {
            val id: String = when(color) {
                PieceColor.Blue -> "BB"
                PieceColor.Red -> "RB"
            }
            pieceCounter++
            val fullID: String = id + pieceCounter
            return Piece(fullID, PieceType.Bo, color)
        }

        fun createFromType(color: PieceColor, type: PieceType): Piece {
            return when(type) {
                PieceType.Po -> createPo(color)
                PieceType.Bo -> createBo(color)
            }
        }
    }

    override fun toString(): String {
        return id
    }

    @DrawableRes
    fun imageResource(): Int {
        return when (type) {
//            PieceType.Po -> if (color is PieceColor.Blue) R.drawable.small_circle else R.drawable.small_cross
//            PieceType.Bo -> if (color is PieceColor.Blue) R.drawable.big_circle else R.drawable.big_cross
            PieceType.Po -> if (color is PieceColor.Blue) R.drawable.white_small_death else R.drawable.black_small_angel
            PieceType.Bo -> if (color is PieceColor.Blue) R.drawable.white_big_death else R.drawable.black_big_angel
        }
    }
}

data class Delta(val x: Int, val y: Int)
data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position): Delta {
        return Delta(this.x + other.x, this.y + other.y)
    }

    operator fun minus(other: Position): Delta {
        return Delta(this.x - other.x, this.y - other.y)
    }

    operator fun plus(other: Delta): Position {
        return Position(this.x + other.x, this.y + other.y)
    }
    fun isSame(other: Position?): Boolean {
        return this === other || ( this.x == other?.x && this.y == other?.y )
    }

    override fun toString(): String {
        return "(${x},${y})"
    }
}

private val INITIAL_BOARD = listOf(
    listOf(null, null, null, null, null, null).map { Piece.pieceOrNullFromString(it) },
    listOf(null, null, null, null, null, null).map { Piece.pieceOrNullFromString(it) },
    listOf(null, null, null, null, null, null).map { Piece.pieceOrNullFromString(it) },
    listOf(null, null, null, null, null, null).map { Piece.pieceOrNullFromString(it) },
    listOf(null, null, null, null, null, null).map { Piece.pieceOrNullFromString(it) },
    listOf(null, null, null, null, null, null).map { Piece.pieceOrNullFromString(it) },
)

private val INITIAL_PIECES = listOf(
    listOf("BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP", ).map { Piece.pieceOrNullFromString(it) },
    listOf("RP", "RP", "RP", "RP", "RP", "RP", "RP", "RP", ).map { Piece.pieceOrNullFromString(it) }
)

data class Board(
    val pieces: List<List<Piece?>> = INITIAL_BOARD,
    val bluePool:List<Piece> = List(8){ Piece.pieceFromString("BP") },
    val redPool:List<Piece> = List(8){ Piece.pieceFromString("RP") },
    val numberBlueBo: Int = 0,
    val numberRedBo: Int = 0
) {
    companion object {
        private val ALL_POSITIONS = (0 until 6).flatMap { y ->
            (0 until 6).map { x -> Position(x, y) }
        }

        fun fromHistory(history: List<Move>): Board {
            var board = Board()
            history.forEach {
                board = board.playAt(it)
            }

            return board
        }
    }

    val allPositions = ALL_POSITIONS
    val allPieces: List<Pair<Position, Piece>> =
        allPositions.mapNotNull { position -> pieces[position.y][position.x]?.let { position to it } }

    fun getPlayerNumberBo(color: PieceColor): Int {
        return when(color) {
            PieceColor.Blue -> numberBlueBo
            else -> numberRedBo
        }
    }

    fun getPlayerPool(color: PieceColor): List<Piece> {
        return when(color) {
            PieceColor.Blue -> bluePool
            else -> redPool
        }
    }

    fun hasTwoTypesInPool(color: PieceColor): Boolean {
        val pool = getPlayerPool(color)
        return pool.first().type != pool.last().type
    }

    fun removeFromPool(piece: Piece): List<Piece>{
        val pool = getPlayerPool(piece.color)
        return when (piece.type) {
            PieceType.Po -> pool.subList(0, pool.size - 1) // remove last element
            PieceType.Bo -> pool.subList(1, pool.size) // remove first element
        }
    }

    fun addInPool(piece: Piece): List<Piece> {
        val pool = getPlayerPool(piece.color)
        return when (piece.type) {
            PieceType.Po -> pool + listOf(piece) // add as last element
            PieceType.Bo -> listOf(piece) + pool // add as first element
        }
    }

    fun isPoolEmpty(player: PieceColor): Boolean = getPlayerPool(player).isEmpty()

    fun hasPieceInPool(color: PieceColor, type: PieceType): Boolean {
        val pool = getPlayerPool(color)
        return type == when (type) {
            PieceType.Po -> pool.last().type
            PieceType.Bo -> pool.first().type
        }
    }

    fun hasPieceInPool(piece: Piece): Boolean = hasPieceInPool(piece.color, piece.type)

    fun getAllEmptyPositions(): List<Position> {
        val allEmptyPositions = mutableListOf<Position>()
        for (position in allPositions) {
            if(pieces[position.y][position.x] == null)
                allEmptyPositions.add(position)
        }
        return allEmptyPositions.toList()
    }

    fun pieceAt(position: Position): Piece? {
        if(!isPositionOnTheBoard(position)) return null
        return pieces.getOrNull(position.y)?.getOrNull(position.x)
    }

    // push a piece on the board (or outside the board)
    // do nothing if 'from' is invalid
    fun slideFromTo(from: Position, to: Position): Board {
        if(!isPositionOnTheBoard(to)) {
            return removePieceAndPutInPool(from)
        }
        else {
            val piece = pieceAt(from) ?: return this
            val newPieces = pieces.map { it.toMutableList() }.toMutableList()
            newPieces[from.y][from.x] = null
            newPieces[to.y][to.x] = piece
            return Board(
                newPieces.map { it.toList() }.toList(),
                this.bluePool,
                this.redPool,
                this.numberBlueBo,
                this.numberRedBo
            )
        }
    }

    fun removePieceAndPutInPool(position: Position): Board {
        val piece = pieceAt(position) ?: return this
        val newPieces  = pieces.map { it.toMutableList() }.toMutableList()
        newPieces[position.y][position.x] = null
        val newPool = addInPool(piece)
        return when(piece.color) {
            PieceColor.Blue -> Board(
                newPieces.map { it.toList() }.toList(),
                newPool,
                this.redPool,
                this.numberBlueBo,
                this.numberRedBo
            )
            PieceColor.Red -> Board(
                newPieces.map { it.toList() }.toList(),
                this.bluePool,
                newPool,
                this.numberBlueBo,
                this.numberRedBo
            )
        }
    }

    fun removePieceAndPromoteIt(position: Position): Board {
        val piece = pieceAt(position) ?: return this
        if( piece.type == PieceType.Bo ) return removePieceAndPutInPool(position)

        val newPieces  = pieces.map { it.toMutableList() }.toMutableList()
        newPieces[position.y][position.x] = null

        val color = piece.color
        val newPool = addInPool(Piece.createBo(color))

        return when(color) {
            PieceColor.Blue -> Board(
                newPieces.map { it.toList() }.toList(),
                newPool,
                this.redPool,
                this.numberBlueBo + 1,
                this.numberRedBo
            )
            PieceColor.Red -> Board(
                newPieces.map { it.toList() }.toList(),
                this.bluePool,
                newPool,
                this.numberBlueBo,
                this.numberRedBo + 1
            )
        }
    }

    fun playAt(piece: Piece, at: Position): Board {
        if(!isPositionOnTheBoard(at)) return this

        val newPool = removeFromPool(piece)
        val newPieces = pieces.map { it.toMutableList() }.toMutableList()
        newPieces[at.y][at.x] = piece
        return when(piece.color) {
            PieceColor.Blue -> Board(
                newPieces.map { it.toList() }.toList(),
                newPool,
                this.redPool
            )
            PieceColor.Red -> Board(
                newPieces.map { it.toList() }.toList(),
                this.bluePool,
                newPool
            )
        }
    }

    fun playAt(move: Move): Board = playAt(move.piece, move.to)
}