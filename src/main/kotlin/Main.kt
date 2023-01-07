import kotlin.math.abs

var x1 = -1
var y1 = -1
var x2 = -1
var y2 = -1

enum class Turn(val letter: String) {
    WHITE("W"), BLACK("B");

    fun otherTurn(): String {
        return if (letter == "W") "B" else "W"
    }
}

fun main() {

    val game = Game()
    val board = Board()
    var turn = Turn.WHITE
    var lastDMove = ""

    while (true) {
        val move = game.getMove(turn)
        if (move == "exit") break

        x1 = 8 - move[1].digitToInt()
        x2 = 8 - move[3].digitToInt()
        y1 = move[0].code - 97
        y2 = move[2].code - 97

        val m = game.checkMove(turn.letter, turn.otherTurn(), board, lastDMove)

        lastDMove = if (m == "yes_double") "$x1$y1" else ""

        if (m.contains("yes")) {
            board.movePawn(move, turn, (m == "yes_spec"))
            turn = if (turn.letter == "W") Turn.BLACK else Turn.WHITE
        }

        if (game.inLastOppRank("W", board.board) || game.noOppPawns("B", board.board)) {
            println("White Wins!")
            break
        } else if (game.inLastOppRank("B", board.board) || game.noOppPawns("W", board.board)) {
            println("Black Wins!")
            break
        } else if (!game.haveValidMove("W", "B", board.board, lastDMove)
            || !game.haveValidMove("B", "W", board.board, lastDMove)) {
            println("Stalemate!")
            break
        }

        println("${game.names[turn.ordinal]}'s turn:")
    }
    println("Bye!")
}


class Game {
    val names = mutableListOf<String>()

    init {
        println("Pawns-Only Chess")
        println("First Player's name:")
        names.add(readln())
        println("Second Player's name:")
        names.add(readln())
    }

    fun getMove(turn: Turn): String {
        println("${names[turn.ordinal]}'s turn:")
        return readln()
    }

    fun checkMove(color: String, oppColor: String, board: Board, lastDMove: String): String {
        val xDifference = if (color == "W") x1 - x2 else x2 - x1

        if (x1 in 0..7 && y1 in 0..7 && x2 in 0..7 && y2 in 0..7) {
            if (y1 == y2) {
                if (board.board[x2][y2] == " " && !(x1 == x2 && board.board[x1][y1] == color)) {
                    if (x1 == x2 || xDifference == 1) {
                        return "yes"
                    }
                    if (xDifference == 2 && x1 == if (color == "W") 6 else 1) {
                        return "yes_double"
                    }
                }
            } else if (abs(y1 - y2) == 1 && xDifference == 1) {
                if (board.board[x2][y2] == oppColor) {
                    return "yes"
                }
                if (checkSpecialCapture(color, oppColor, board.board, lastDMove)) {
                    return "yes_spec"
                }
            }
        }
        println("Invalid Input")
        return "no"
    }

    private fun checkSpecialCapture(color: String, oppColor: String, board: MutableList<MutableList<String>>,
                                    lastDMove: String, x: Int = x2, y: Int = y2): Boolean {
        return if (color == "W")
            (board[x][y] == " " && board[x + 1][y] == oppColor && lastDMove == "${x - 1}$y")
        else
            (board[x][y] == " " && board[x - 1][y] == oppColor && lastDMove == "${x + 1}$y")
    }

    fun inLastOppRank(color: String, board: MutableList<MutableList<String>>): Boolean {
        for (i in 0..7) {
            if (board[0][i] == color || board[7][i] == color)
                return true
        }
        return false
    }

    fun noOppPawns(oppColor: String, board: MutableList<MutableList<String>>): Boolean {
        for (x in 1..6) {
            for (y in 0..7) {
                if (board[x][y] == oppColor) {
                    return false
                }
            }
        }
        return true
    }

    fun haveValidMove(color: String, oppColor: String, board: MutableList<MutableList<String>>, lastDMove: String): Boolean {
        val xDiff = if (color == "W") -1 else 1
        for (x in 0..7) {
            for (y in 0..7) {
                if (board[x][y] == color) {
                    if (y == 0 || y == 7) {
                        if (board[x + xDiff][y] == " " || board[x + xDiff][y + (1 - y / 3)] == oppColor
                            || checkSpecialCapture(color, oppColor, board, lastDMove, x + xDiff, y + (1 - y / 3))) {
                            return true
                        }
                    } else if (board[x + xDiff][y] == " "
                        || board[x + xDiff][y + 1] == oppColor || board[x + xDiff][y - 1] == oppColor
                        || checkSpecialCapture(color, oppColor, board, lastDMove, x + xDiff, y - 1)
                        || checkSpecialCapture(color, oppColor, board, lastDMove, x + xDiff, y + 1)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}


class Board {
    val board = mutableListOf(
        MutableList(8) { " " },
        MutableList(8) { "B" },
        MutableList(8) { " " },
        MutableList(8) { " " },
        MutableList(8) { " " },
        MutableList(8) { " " },
        MutableList(8) { "W" },
        MutableList(8) { " " }
    )

    init { printBoard() }

    fun movePawn(move: String, turn: Turn, specCapt: Boolean = false) {
        if (board[x1][y1] == turn.letter) {
            board[x1][y1] = " "
            board[x2][y2] = turn.letter
            if (specCapt) {
                board[x2 + (if (turn.letter == "W") 1 else -1)][y2] = " "
            }
            printBoard()
        } else {
            println("No ${turn.name.lowercase()} pawn at ${move[0]}${move[1]}")
        }
    }

    private fun printBoard() {
        val rowBorder = "  +---+---+---+---+---+---+---+---+"
        val columnBorder = "    a   b   c   d   e   f   g   h"

        for (row in 0..7) {
            println(rowBorder)
            print(8 - row)
            for (column in 0..7)
                print(" | " + board[row][column])
            println(" |")
        }
        println(rowBorder)
        println(columnBorder)
    }
}
