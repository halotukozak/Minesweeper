package minesweeper

import kotlin.random.Random

const val SIZE = 9

fun main() {
    println("How many mines do you want on the field?")
    val numberOfMines = readln().toInt()

    println(Board(SIZE))

    println("Set/unset mine marks or claim a cell as free:")
    val (x, y, _) = readln().split(" ")
    val board = Board(SIZE, numberOfMines, mapOf("x" to x.toInt() - 1, "y" to y.toInt() - 1))

    while (board.isNotWon()) {
        println("Set/unset mine marks or claim a cell as free:")
        val (x, y, action) = readln().split(" ")

        val cannotBeMine = Pair(x.toInt() - 1, y.toInt() - 1)

        when (action) {
            "free" -> {
                if (board.checkCell(cannotBeMine.first, cannotBeMine.second)) board.endGame()
                else board.explore(cannotBeMine.first, cannotBeMine.second)
            }
            "mine" -> {
                board.toggleMarking(cannotBeMine.first, cannotBeMine.second)
            }
        }
        println(board)
    }
    println(board)
    println("Congratulations! You found all the mines!")
}

data class Cell(private val x: Int, private val y: Int, var isMine: Boolean = false) {
    private var number = 0
    var isMarked = false
    var isExplored = false

    fun add() = number++

    fun notHasMinesAround(): Boolean = number > 0

    fun makeMine() {
        isMine = true
    }

    fun toggleMark() {
        isMarked = !isMarked
    }

    fun explore() {
        isExplored = true
        isMarked = false
    }

    fun showMine() {
        if (isMine) explore()
    }

    override fun toString(): String {
        return if (isMarked) "*" else if (!isExplored) "." else if (isMine) "X" else if (number == 0) "/" else number.toString()
    }

    fun isNotMine(): Boolean = !isMine

}

data class Board(
    override val size: Int, val numberOfMines: Int = 0, val cannotBeMine: Map<String, Int> = emptyMap()
) : MutableList<MutableList<Cell>> by mutableListOf(mutableListOf()) {

    private val mines = mutableSetOf<Cell>()
    private val markedCells = mines
    private val unExploredCells = mines

    init {
        this.clear()
        for (x in indices) {
            val line = mutableListOf<Cell>()
            for (y in indices) {
                line.add(Cell(x, y))
                unExploredCells.add(line[y])
            }
            this.add(line)
        }
        setMines()
        if (cannotBeMine.isNotEmpty()) this[cannotBeMine["x"]!!][cannotBeMine["y"]!!].explore()

    }


    private fun setMines() {
        var minesToSet = numberOfMines

        while (minesToSet > 0) {
            val x = Random.nextInt(0, SIZE)
            val y = Random.nextInt(0, SIZE)
            val cell = this[x][y]

            if (!cell.isMine && !(x == cannotBeMine["x"] && y == cannotBeMine["y"])) {
                if (!cell.isMine) {
                    cell.makeMine()
                    minesToSet--
                    mines.add(cell)
                    for (row in arrayOf(x - 1, x, x + 1)) {
                        if (row in indices) for (column in arrayOf(
                            y - 1, y, y + 1
                        )) if (column in indices) this[row][column].add()
                    }
                }
            }
        }
    }

    fun checkCell(x: Int, y: Int): Boolean = this[x][y].isMine

    fun toggleMarking(x: Int, y: Int) {
        val y = y - 1
        val cell = this[x][y]
        cell.toggleMark()
        if (cell.isMarked) markedCells.add(cell)
        else markedCells.remove(cell)

    }


    fun endGame() {
        this.forEach { it -> it.forEach { it.showMine() } }
        println("You stepped on a mine and failed!")
    }

    fun explore(x: Int, y: Int) {
        val cell = this[x][y]
        if (!cell.isExplored) {
            cell.explore()
            unExploredCells.remove(cell)

            for (row in arrayOf(x - 1, x, x + 1)) {
                if (row in this.indices) for (column in arrayOf(y - 1, y, y + 1)) {
                    if (column in this.indices && cell.isNotMine() && cell.notHasMinesAround()) this[row][column].explore()
                }
            }
        }
    }

    fun isNotWon(): Boolean = !(markedCells == mines || unExploredCells == mines)

    override fun toString(): String {
        var output = " │123456789│\n—│—————————│\n"
        for (i in indices) output += this[i].joinToString(separator = "", prefix = "${i + 1}│", postfix = "|\n")
        output += "—│—————————│"
        return output

    }
}
