package com.example.snake

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.SparseArray
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.util.set
import stanford.androidlib.graphics.*
import java.util.Random



class SnakeCanvas(context: Context, attrs: AttributeSet)
    : GCanvas(context, attrs), View.OnTouchListener, GestureDetector.OnGestureListener {

    // Snake Gamplay Boolean Constants
    // current values will be replaced by settings
    private var hidePauseButton = false
    private var startGameOnPause = false
    private var showGridSquares = false

    // Snake Gameplay Settings Constants
    // current values will be replaced by settings
    private var nGridSquaresPerRow = 20
    private var snakeStartLength = 5
    private var fps = 5

    // Snake Gameplay Static Constants
    private val nGridSquaresLeftOfSnakeStart = 2
    private val percentDownGridForStartRow = 0.70
    private val percentOfWidthTheGridTakes = 0.95
    private val percentOfRemainingHeightBelowGridTheButtonsTake = 0.85
    private val percentOfButtonsWidthForButtonsGap = 0.1
    private val percentSizeOfSnakeImageToGridSquareSize = 1.10

    // Snake Gameplay Instance Variables
    private var snakeGridSquareSize = 0f
    private var indexOfHeadSnakeNode = 0
    private var indexOfTailSnakeNode = 0
    private var lastSnakeNodeIndex = 0
    private var currentFoodColumn = -1
    private var currentFoodRow = -1
    private var currentBonusColumn = -1
    private var currentBonusRow = -1
    private val frontNodeIndex = -1
    private var snakeNodeMap = SparseArray<SnakeNode>()
    private var snakeSprites = ArrayList<GSprite>()

    // Snake Scoring Instance Variables
    private var snakeMoveCounter = 0
    private var score = 0
    private val scoreLabel = GLabel("score: 0")
    private val scoreLabelFontSize = 70f
    private val scorePerFoodEaten = 50
    private val scorePerBonusEaten = 200
    private val nToIncreaseBonusStaysVisible = 0.2
    private val nToIncreaseBonusAppears = 1
    private val bonusVisibilityMultiplier = 0.7
    private val bonusFrequencyMultiplier = 5
    private var nMovesBonusAppears = 100.0
    private var nMovesBonusDisappears = 20.0
    private var nMovesBonusVisible = 0
    private var bonusIsVisible = false

    // Snake Grid Array Variables
    private lateinit var snakeGridArray : Array<Array<Int>>
    private val emptySpaceIndex = 0
    private val snakeSpaceIndex = 1
    private val foodSpaceIndex = 2
    private val bonusSpaceIndex = 3

    // Snake Directions
    private val up = "up"
    private val left = "left"
    private val right = "right"
    private val down = "down"

    // Snake Grid Color ARGB Values
    private val gridColorA = 255
    private val gridColorR = 182
    private val gridColorG = 240
    private val gridColorB = 200

    // Snake Game Setup Instance Variables
    private lateinit var snakeGrid: GRect
    private lateinit var upButton: GSprite
    private lateinit var leftButton: GSprite
    private lateinit var rightButton: GSprite
    private lateinit var downButton: GSprite
    private lateinit var pauseNPlayButton: GSprite
    private lateinit var pauseButtonImage: Bitmap
    private lateinit var playButtonImage: Bitmap

    // Snake Images Instance Variables
    private lateinit var snakeHeadUpImage: Bitmap
    private lateinit var snakeHeadLeftImage: Bitmap
    private lateinit var snakeHeadRightImage: Bitmap
    private lateinit var snakeHeadDownImage: Bitmap
    private lateinit var snakeBodyHorizontalImage: Bitmap
    private lateinit var snakeBodyVerticalImage: Bitmap
    private lateinit var snakeTailUpImage: Bitmap
    private lateinit var snakeTailLeftImage: Bitmap
    private lateinit var snakeTailRightImage: Bitmap
    private lateinit var snakeTailDownImage: Bitmap
    private lateinit var snakeCornerUpLeftImage: Bitmap
    private lateinit var snakeCornerUpRightImage: Bitmap
    private lateinit var snakeCornerDownLeftImage: Bitmap
    private lateinit var snakeCornerDownRightImage: Bitmap

    // Food Images Instance Variables
    private lateinit var foodCheeseImage: Bitmap
    private lateinit var foodBeerImage: Bitmap
    private lateinit var foodHamburgerImage: Bitmap
    private lateinit var foodPizzaImage: Bitmap
    private lateinit var foodSushiImage: Bitmap
    private lateinit var snakeFood: GSprite
    private lateinit var bonusFood: GSprite

    // Snake Boolean Instance Variables
    private var gameIsPaused = false
    private var gameOver = false
    private var movingUp = false
    private var movingLeft = false
    private var movingRight = true
    private var movingDown = false

    // Gesture Detector
    private lateinit var mGestureDetector : GestureDetector

    // Snake Activity
    private lateinit var activity: SnakeActivity

    // Game Over Dialog Boolean
    private var dialogIsOpen = false

    override fun init() {
        if(startGameOnPause) gameIsPaused = true
        mGestureDetector = GestureDetector(activity, this)
        val bgColor = Paint()
        bgColor.setARGB(255, 77,77,77)
        backgroundColor = bgColor
        setupGame()
        startGame()
    }

    fun passActivity(activity: SnakeActivity) {
        this.activity = activity
    }

    fun updateConstants(newHidePauseButton: Boolean, newStartGameOnPause: Boolean,
                        newShowGridSquares: Boolean, newNGridSquaresPerRow: Int,
                        newSnakeStartLength: Int, newFps: Int) {
        hidePauseButton = newHidePauseButton
        startGameOnPause = newStartGameOnPause
        showGridSquares = newShowGridSquares
        nGridSquaresPerRow = newNGridSquaresPerRow
        snakeStartLength = newSnakeStartLength
        fps = newFps

        // update how often bonus food appears & stays visible
        resetBonusFrequencies()

        // increase bonus frequencies by how long snake length starts
        for(i in 0 until snakeStartLength)
            increaseBonusFrequencies()
    }

    private fun resetBonusFrequencies() {
        nMovesBonusDisappears = nGridSquaresPerRow * bonusVisibilityMultiplier
        nMovesBonusAppears = nMovesBonusDisappears * bonusFrequencyMultiplier
    }

    private fun increaseBonusFrequencies() {
        nMovesBonusDisappears += nToIncreaseBonusStaysVisible
        nMovesBonusAppears += nToIncreaseBonusAppears
    }

    private fun startGame() {
        animate(fps) {
            if(!gameIsPaused && !gameOver) {
                moveSnake()
            }

            // when game over, show dialog box to submit high score
            // and restart new game; also add button to restart game
            if(gameOver && !dialogIsOpen) {
                dialogIsOpen = true
                activity.openGameOverDialog(score)
            }
        }
    }

    private fun moveSnake() {

        // First grabs data for current head of snake
        val headIndex = indexOfHeadSnakeNode
        val headNode = snakeNodeMap[indexOfHeadSnakeNode]
        val headSprite = snakeSprites[indexOfHeadSnakeNode]
        indexOfHeadSnakeNode = indexOfTailSnakeNode
        headNode.nextNode = indexOfHeadSnakeNode

        // grabs the row & column of head in grid
        val rowOfHead = headNode.gridRow
        val colOfHead = headNode.gridColumn

        // Second grabs data for current tail of snake
        val tailIndex = indexOfTailSnakeNode
        val tailNode = snakeNodeMap[indexOfTailSnakeNode]
        val tailSprite = snakeSprites[indexOfTailSnakeNode]
        val originalTailNodeDirection = tailNode.direction
        indexOfTailSnakeNode = tailNode.nextNode
        tailNode.nextNode = frontNodeIndex

        // grabs the row & column of tail in grid
        val rowOfTail = tailNode.gridRow
        val colOfTail = tailNode.gridColumn

        // marks that row & column as an empty space
        snakeGridArray[rowOfTail-1][colOfTail-1] = emptySpaceIndex

        // replaces head sprite with a body part sprite
        headSprite.bitmap = useDirectionToReplaceHeadImage(headNode.direction)

        // grabs row & column of next grid square based on
        // which directional button was last clicked;
        // also changes direction for nodes and
        // changes tail sprite to be the new snake head
        var rowOfNewHead = 0
        var colOfNewHead = 0
        if(movingUp) {
            rowOfNewHead = rowOfHead-1
            colOfNewHead = colOfHead
            headNode.direction = up
            tailNode.direction = up
            tailSprite.bitmap = snakeHeadUpImage
        }
        if(movingLeft) {
            rowOfNewHead = rowOfHead
            colOfNewHead = colOfHead-1
            headNode.direction = left
            tailNode.direction = left
            tailSprite.bitmap = snakeHeadLeftImage
        }
        if(movingRight) {
            rowOfNewHead = rowOfHead
            colOfNewHead = colOfHead+1
            headNode.direction = right
            tailNode.direction = right
            tailSprite.bitmap = snakeHeadRightImage
        }
        if(movingDown) {
            rowOfNewHead = rowOfHead+1
            colOfNewHead = colOfHead
            headNode.direction = down
            tailNode.direction = down
            tailSprite.bitmap = snakeHeadDownImage
        }

        // checks first to see if snake has moved out of bounds or if snake has eaten itself
        // if so, then game over
        if(rowOfNewHead < 1 || rowOfNewHead > nGridSquaresPerRow ||
            colOfNewHead < 1 || colOfNewHead > nGridSquaresPerRow ||
            snakeGridArray[rowOfNewHead-1][colOfNewHead-1] == snakeSpaceIndex) {
            gameOver = true
            remove(tailSprite)
        }

        // Checks if snake ate food or bonus food
        var snakeAteFood = false
        var snakeAteBonus = false
        if(!gameOver && snakeGridArray[rowOfNewHead - 1][colOfNewHead - 1] == foodSpaceIndex)
            snakeAteFood = true
        if(!gameOver && snakeGridArray[rowOfNewHead - 1][colOfNewHead - 1] == bonusSpaceIndex) {
            snakeAteFood = true
            snakeAteBonus = true
            resetBonusFood()
        }

        // if snake is still alive, continue moving snake to next grid square
        if(!gameOver) {
            snakeGridArray[rowOfNewHead - 1][colOfNewHead - 1] = snakeSpaceIndex
            tailSprite.moveTo(getSnakeGridPoint(rowOfNewHead, colOfNewHead))

            // set new row & column of tail node
            tailNode.gridRow = rowOfNewHead
            tailNode.gridColumn = colOfNewHead

            // update respective nodes
            snakeNodeMap[tailIndex] = tailNode
            snakeNodeMap[headIndex] = headNode
        }

        // initializes boolean to check if snake has cleared the board
        var snakeHasClearedTheBoard = false

        // if snake ate food, then snake grows longer and scores one point
        if(!gameOver && snakeAteFood) {

            // each time snake eats food, the duration of bonus food visibility
            // and the wait time for the bonus food to appear increases
            increaseBonusFrequencies()

            // create new snake sprite & node to grow snake
            val newSnakeSprite = GSprite(snakeBodyHorizontalImage)
            val newSnakeNode = SnakeNode()

            // set snake node variables
            newSnakeNode.direction = originalTailNodeDirection
            newSnakeNode.nextNode = indexOfTailSnakeNode
            newSnakeNode.gridRow = rowOfTail
            newSnakeNode.gridColumn = colOfTail

            // set appropriate variables and add sprite to screen
            snakeGridArray[rowOfTail-1][colOfTail-1] = snakeSpaceIndex
            indexOfTailSnakeNode = ++lastSnakeNodeIndex
            snakeNodeMap.put(lastSnakeNodeIndex, newSnakeNode)
            snakeSprites.add(newSnakeSprite)
            add(newSnakeSprite, getSnakeGridPoint(rowOfTail, colOfTail))

            // checks if snake has cleared the board
            when {
                snakeSprites.size == nGridSquaresPerRow * nGridSquaresPerRow ->
                    snakeHasClearedTheBoard = true
                snakeAteBonus -> {
                    // bonus food eaten
                    score += scorePerBonusEaten
                    updateScore()
                }
                else -> {
                    // normal food eaten
                    score += scorePerFoodEaten
                    updateScore()
                    addFoodToGrid()
                }
            }
        }

        // If bonus is not already visible, count snake move
        // and if snake move counter reaches amount for bonus food to appear
        // then bonus food will be added to the grid
        if(!bonusIsVisible) snakeMoveCounter++
        if(snakeMoveCounter >= nMovesBonusAppears) {
            snakeMoveCounter = 0
            bonusIsVisible = true

            // checks if there is still room to add bonus food to grid
            if(snakeSprites.size <= nGridSquaresPerRow * nGridSquaresPerRow - 2)
                addBonusToGrid()
        }

        // If bonus food has been visible for longer than determined amount
        // then bonus food disappears from snake grid
        if(nMovesBonusVisible >= nMovesBonusDisappears) {
            snakeGridArray[currentBonusRow-1][currentBonusColumn-1] = emptySpaceIndex
            resetBonusFood()
        } else if(bonusIsVisible) {
            nMovesBonusVisible++
        }

        // changes the sprite of the new tail
        val newTailNode = snakeNodeMap[indexOfTailSnakeNode]
        val newTailSprite = snakeSprites[indexOfTailSnakeNode]
        when (newTailNode.direction) {
            up -> newTailSprite.bitmap = snakeTailDownImage
            down -> newTailSprite.bitmap = snakeTailUpImage
            left -> newTailSprite.bitmap = snakeTailRightImage
            right -> newTailSprite.bitmap = snakeTailLeftImage
        }

        // if snake has cleared the board, game is over
        if(snakeHasClearedTheBoard) gameOver = true
    }

    private fun resetBonusFood() {
        nMovesBonusVisible = 0
        snakeMoveCounter = 0
        bonusIsVisible = false
        bonusFood.moveTo(width.toFloat(), height.toFloat())
        currentBonusRow = -1
        currentBonusColumn = -1
    }

    // changes current head sprite into appropriate body sprite
    private fun useDirectionToReplaceHeadImage(direction: String): Bitmap {
        when {
            movingUp -> {
                when(direction) {
                    up -> return snakeBodyVerticalImage
                    left -> return snakeCornerUpRightImage
                    right -> return snakeCornerUpLeftImage
                }
            }
            movingLeft -> {
                when(direction) {
                    up -> return snakeCornerDownLeftImage
                    left -> return snakeBodyHorizontalImage
                    down -> return snakeCornerUpLeftImage
                }
            }
            movingRight -> {
                when(direction) {
                    up -> return snakeCornerDownRightImage
                    right -> return snakeBodyHorizontalImage
                    down -> return snakeCornerUpRightImage
                }
            }
            movingDown -> {
                when(direction) {
                    left -> return snakeCornerDownRightImage
                    right -> return snakeCornerDownLeftImage
                    down -> return snakeBodyVerticalImage
                }
            }
        }
        return snakeBodyHorizontalImage
    }

    private fun updateScore() {
        scoreLabel.text = "score: $score"
        val scoreLabelOffsetEdges = (1 - percentOfWidthTheGridTakes) * width / 2
        scoreLabel.moveTo((width - scoreLabel.width - scoreLabelOffsetEdges).toFloat(),
            (snakeGrid.x + snakeGrid.height + scoreLabelOffsetEdges).toFloat())
    }

    fun retry() {

        // remove snake & food sprites
        remove(snakeFood)
        for(sprite in snakeSprites)
            remove(sprite)

        // reset to starting position
        movingUp = false
        movingRight = true
        movingDown = false
        movingLeft = false

        // set score to 0
        score = 0
        updateScore()

        // reset instance variables
        lastSnakeNodeIndex = 0
        snakeNodeMap = SparseArray()
        snakeSprites = ArrayList()
        addSnakeToGrid()

        // add food back to grid
        add(snakeFood, width.toFloat(), height.toFloat())
        addFoodToGrid()

        // reset bonus food instance variables
        snakeMoveCounter = 0
        resetBonusFrequencies()
        resetBonusFood()

        // reset booleans
        gameOver = false
        dialogIsOpen = false
        if(startGameOnPause) {
            pauseNPlayButton.bitmap = playButtonImage
            gameIsPaused = true
        }
    }

    /* Code below sets up snake game */

    private fun setupGame() {
        setupSnakeGrid()
        if(showGridSquares)
            setupGridSquares()
        setupScoreLabel()
        setupSnake()
        setupButtons()
    }

    private fun setupSnakeGrid() {

        // set values
        val snakeGridSize = percentOfWidthTheGridTakes * width
        val gridLocation = (width - snakeGridSize) / 2
        snakeGrid = GRect(gridLocation.toFloat(), gridLocation.toFloat(), snakeGridSize.toFloat(), snakeGridSize.toFloat())
        snakeGridSquareSize = snakeGrid.width / nGridSquaresPerRow

        // color grid
        val gridColor = Paint()
        gridColor.setARGB(gridColorA, gridColorR, gridColorG, gridColorB)
        snakeGrid.fillColor = gridColor

        add(snakeGrid)
    }

    private fun setupGridSquares() {
        val transparentColor = Paint()
        transparentColor.setARGB(0, 0, 0, 0)
        val rGBColorDifference = 10
        val gridSquareColor1 = Paint()
        gridSquareColor1.setARGB(gridColorA, gridColorR-rGBColorDifference,
            gridColorG-rGBColorDifference, gridColorB-rGBColorDifference)
        val gridSquareColor2 = Paint()
        gridSquareColor2.setARGB(gridColorA, gridColorR, gridColorG, gridColorB)

        for(row in 1..nGridSquaresPerRow) {
            for(column in 1..nGridSquaresPerRow) {
                if(row % 2 == 0) {
                    if(column % 2 == 0)
                        addGridSquare(row, column, gridSquareColor1, transparentColor)
                    else addGridSquare(row, column, gridSquareColor2, transparentColor)
                } else {
                    if(column % 2 != 0)
                        addGridSquare(row, column, gridSquareColor1, transparentColor)
                    else addGridSquare(row, column, gridSquareColor2, transparentColor)
                }
            }
        }
    }

    private fun addGridSquare(row: Int, column: Int, gridSquareColor: Paint, transparentColor: Paint) {
        val gridSquare = GRect(snakeGridSquareSize, snakeGridSquareSize)
        gridSquare.color = transparentColor
        gridSquare.fillColor = gridSquareColor

        val gridSquarePointX = snakeGrid.x + (row - 1) * snakeGridSquareSize
        val gridSquarePointY = snakeGrid.y + (column - 1) * snakeGridSquareSize
        val gridSquarePoint = GPoint(gridSquarePointX, gridSquarePointY)

        add(gridSquare, gridSquarePoint)
    }

    private fun setupScoreLabel() {
        val scoreLabelPaint = Paint()
        scoreLabelPaint.setARGB(gridColorA, gridColorR, gridColorG, gridColorB)
        scoreLabel.paint = scoreLabelPaint
        scoreLabel.fontSize = scoreLabelFontSize
        val scoreLabelOffsetEdges = (1 - percentOfWidthTheGridTakes) * width / 2
        add(scoreLabel, (width - scoreLabel.width - scoreLabelOffsetEdges).toFloat(),
            (snakeGrid.x + snakeGrid.height + scoreLabelOffsetEdges).toFloat())
    }

    private fun setupSnake() {
        setupSnakeAndFoodImages()
        addSnakeToGrid()
        addFoodToGrid()
    }

    private fun setupSnakeAndFoodImages() {

        // Slice image strip to get snake images
        val snakeImageStrip = BitmapFactory.decodeResource(resources, R.drawable.snake_images)
        snakeHeadUpImage = getSnakeImage(1, 4, snakeImageStrip)
        snakeHeadLeftImage = getSnakeImage(2, 4, snakeImageStrip)
        snakeHeadRightImage = getSnakeImage(1, 5, snakeImageStrip)
        snakeHeadDownImage = getSnakeImage(2, 5, snakeImageStrip)
        snakeBodyHorizontalImage = getSnakeImage(1, 2, snakeImageStrip)
        snakeBodyVerticalImage = getSnakeImage(2, 3, snakeImageStrip)
        snakeTailUpImage = getSnakeImage(4, 5, snakeImageStrip)
        snakeTailLeftImage = getSnakeImage(3, 5, snakeImageStrip)
        snakeTailRightImage = getSnakeImage(4, 4, snakeImageStrip)
        snakeTailDownImage = getSnakeImage(3, 4, snakeImageStrip)
        snakeCornerUpLeftImage = getSnakeImage(3, 3, snakeImageStrip)
        snakeCornerUpRightImage = getSnakeImage(2, 1, snakeImageStrip)
        snakeCornerDownLeftImage = getSnakeImage(1, 3, snakeImageStrip)
        snakeCornerDownRightImage = getSnakeImage(1, 1, snakeImageStrip)
        var snakeFoodImage = getSnakeImage(4, 1, snakeImageStrip)

        // resize snake images
        snakeHeadUpImage = resizeSnakeImageToSnakeGridSquareSize(snakeHeadUpImage)
        snakeHeadLeftImage = resizeSnakeImageToSnakeGridSquareSize(snakeHeadLeftImage)
        snakeHeadRightImage = resizeSnakeImageToSnakeGridSquareSize(snakeHeadRightImage)
        snakeHeadDownImage = resizeSnakeImageToSnakeGridSquareSize(snakeHeadDownImage)
        snakeBodyHorizontalImage = resizeSnakeImageToSnakeGridSquareSize(snakeBodyHorizontalImage)
        snakeBodyVerticalImage = resizeSnakeImageToSnakeGridSquareSize(snakeBodyVerticalImage)
        snakeTailUpImage = resizeSnakeImageToSnakeGridSquareSize(snakeTailUpImage)
        snakeTailLeftImage = resizeSnakeImageToSnakeGridSquareSize(snakeTailLeftImage)
        snakeTailRightImage = resizeSnakeImageToSnakeGridSquareSize(snakeTailRightImage)
        snakeTailDownImage = resizeSnakeImageToSnakeGridSquareSize(snakeTailDownImage)
        snakeCornerUpLeftImage = resizeSnakeImageToSnakeGridSquareSize(snakeCornerUpLeftImage)
        snakeCornerUpRightImage = resizeSnakeImageToSnakeGridSquareSize(snakeCornerUpRightImage)
        snakeCornerDownLeftImage = resizeSnakeImageToSnakeGridSquareSize(snakeCornerDownLeftImage)
        snakeCornerDownRightImage = resizeSnakeImageToSnakeGridSquareSize(snakeCornerDownRightImage)
        snakeFoodImage = resizeSnakeImageToSnakeGridSquareSize(snakeFoodImage)

        // initialize food sprite and add outside screen
        snakeFood = GSprite(snakeFoodImage)
        add(snakeFood, width.toFloat(), height.toFloat())

        // initialize bonus food images & sprite and add sprite outside screen
        foodCheeseImage = BitmapFactory.decodeResource(resources, R.drawable.foodcheese)
        foodBeerImage = BitmapFactory.decodeResource(resources, R.drawable.foodbeer)
        foodHamburgerImage = BitmapFactory.decodeResource(resources, R.drawable.foodhamburger)
        foodPizzaImage = BitmapFactory.decodeResource(resources, R.drawable.foodpizza)
        foodSushiImage = BitmapFactory.decodeResource(resources, R.drawable.foodsushi)

        foodCheeseImage = resizeSnakeImageToSnakeGridSquareSize(foodCheeseImage)
        foodBeerImage = resizeSnakeImageToSnakeGridSquareSize(foodBeerImage)
        foodHamburgerImage = resizeSnakeImageToSnakeGridSquareSize(foodHamburgerImage)
        foodPizzaImage = resizeSnakeImageToSnakeGridSquareSize(foodPizzaImage)
        foodSushiImage = resizeSnakeImageToSnakeGridSquareSize(foodSushiImage)
        bonusFood = GSprite(foodCheeseImage)
        add(bonusFood, width.toFloat(), height.toFloat())
    }

    // Slices strip to get the desired snake image based on the row & column of strip grid square
    private fun getSnakeImage(row: Int, column: Int, snakeImageStrip: Bitmap): Bitmap {
        val nStripRows = 4
        val nStripColumns = 5
        val stripAreaWidth = snakeImageStrip.width / nStripColumns
        val stripAreaHeight = snakeImageStrip.height / nStripRows
        return Bitmap.createBitmap(snakeImageStrip,
            (column - 1) * stripAreaWidth, (row - 1) * stripAreaHeight,
            stripAreaWidth, stripAreaHeight)
    }

    private fun resizeSnakeImageToSnakeGridSquareSize(snakeImage: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(snakeImage,
            (snakeGridSquareSize * percentSizeOfSnakeImageToGridSquareSize).toInt(),
            (snakeGridSquareSize * percentSizeOfSnakeImageToGridSquareSize).toInt(),
            false)
    }

    private fun addSnakeToGrid() {

        createAndStartTrackingSnakeSprites()

        snakeGridArray = Array(nGridSquaresPerRow) {Array(nGridSquaresPerRow) {emptySpaceIndex} }
        val rowOfGridForSnakeToStart = (nGridSquaresPerRow * percentDownGridForStartRow).toInt()
        var columnOfGridForSnakeToStart = nGridSquaresLeftOfSnakeStart + snakeStartLength

        for(i in 0 until snakeSprites.size) {
            val sprite = snakeSprites[i]
            snakeGridArray[rowOfGridForSnakeToStart-1][columnOfGridForSnakeToStart-1] = snakeSpaceIndex

            // update snake node with location of node in snake grid
            val node = snakeNodeMap[i]
            node.gridRow = rowOfGridForSnakeToStart
            node.gridColumn = columnOfGridForSnakeToStart
            snakeNodeMap[i] = node

            add(sprite, getSnakeGridPoint(rowOfGridForSnakeToStart, columnOfGridForSnakeToStart--))
        }
    }

    private fun addFoodToGrid() {
        var row = 0
        var col = 0
        var foodSpaceChosen = false
        while(!foodSpaceChosen) {
            row = Random().nextInt(nGridSquaresPerRow) + 1
            col = Random().nextInt(nGridSquaresPerRow) + 1
            if (snakeGridArray[row - 1][col - 1] == emptySpaceIndex) foodSpaceChosen = true
            if(row == currentFoodRow && col == currentFoodColumn) foodSpaceChosen = false
        }
        currentFoodRow = row
        currentFoodColumn = col
        snakeFood.moveTo(getSnakeGridPoint(row, col))
        snakeGridArray[row-1][col-1] = foodSpaceIndex
    }

    private fun addBonusToGrid() {
        updateBonusImage()
        var row = 0
        var col = 0
        var bonusSpaceChosen = false
        while(!bonusSpaceChosen) {
            row = Random().nextInt(nGridSquaresPerRow) + 1
            col = Random().nextInt(nGridSquaresPerRow) + 1
            if (snakeGridArray[row - 1][col - 1] == emptySpaceIndex) bonusSpaceChosen = true
            if(row == currentBonusRow && col == currentBonusColumn) bonusSpaceChosen = false
        }
        currentBonusRow = row
        currentBonusColumn = col
        bonusFood.moveTo(getSnakeGridPoint(row, col))
        snakeGridArray[row-1][col-1] = bonusSpaceIndex
    }

    private fun updateBonusImage() {
        when(Random().nextInt(5) + 1) {
            1 -> bonusFood.bitmap = foodCheeseImage
            2 -> bonusFood.bitmap = foodBeerImage
            3 -> bonusFood.bitmap = foodHamburgerImage
            4 -> bonusFood.bitmap = foodPizzaImage
            5 -> bonusFood.bitmap = foodSushiImage
        }
    }

    // Creates the first instance of the snake and starts tracking its SnakeNodes
    private fun createAndStartTrackingSnakeSprites() {

        // Creates and tracks snake head node
        val snakeHead = GSprite(snakeHeadRightImage)
        val snakeHeadNode = SnakeNode()
        snakeHeadNode.direction = right
        snakeHeadNode.nextNode = frontNodeIndex
        snakeNodeMap.put(lastSnakeNodeIndex, snakeHeadNode)
        snakeSprites.add(snakeHead)
        indexOfHeadSnakeNode = lastSnakeNodeIndex

        // Creates and tracks snake body nodes
        val nSnakeBodySprites = snakeStartLength - 2
        for(i in 0 until nSnakeBodySprites) {
            val snakeBody = GSprite(snakeBodyHorizontalImage)
            val snakeBodyNode = SnakeNode()
            snakeBodyNode.direction = right
            snakeBodyNode.nextNode = lastSnakeNodeIndex++
            snakeNodeMap.put(lastSnakeNodeIndex, snakeBodyNode)
            snakeSprites.add(snakeBody)
        }

        // Creates and tracks snake tail node
        val snakeTail = GSprite(snakeTailLeftImage)
        val snakeTailNode = SnakeNode()
        snakeTailNode.direction = right
        snakeTailNode.nextNode = lastSnakeNodeIndex++
        snakeNodeMap.put(lastSnakeNodeIndex, snakeTailNode)
        snakeSprites.add(snakeTail)
        indexOfTailSnakeNode = lastSnakeNodeIndex
    }

    // SnakeNode Class tracks the direction, grid row & column,
    // & the index of the next SnakeNode it's connected to
    class SnakeNode {
        lateinit var direction: String
        var gridColumn = 0
        var gridRow = 0
        var nextNode = 0
    }

    // returns the GPoint in the canvas based on the row & column of desired grid square
    private fun getSnakeGridPoint(row: Int, column: Int): GPoint {
        val x = snakeGrid.x + (column - 1) * snakeGridSquareSize
        val y = snakeGrid.y + (row - 1) * snakeGridSquareSize
        return GPoint(x, y)
    }

    private fun setupButtons() {

        // Get Button Images
        var upButtonImage = BitmapFactory.decodeResource(resources, R.drawable.up_button)
        var leftButtonImage = BitmapFactory.decodeResource(resources, R.drawable.left_button)
        var rightButtonImage = BitmapFactory.decodeResource(resources, R.drawable.right_button)
        var downButtonImage = BitmapFactory.decodeResource(resources, R.drawable.down_button)
        pauseButtonImage = BitmapFactory.decodeResource(resources, R.drawable.pause_button)
        playButtonImage = BitmapFactory.decodeResource(resources, R.drawable.play_button)

        // Scale Buttons
        val remainingHeightBelowGrid = height - width
        val buttonSize = ((remainingHeightBelowGrid *
                percentOfRemainingHeightBelowGridTheButtonsTake) / 3).toFloat()
        upButtonImage = upButtonImage.scaleToHeight(buttonSize)
        leftButtonImage = leftButtonImage.scaleToHeight(buttonSize)
        rightButtonImage = rightButtonImage.scaleToHeight(buttonSize)
        downButtonImage = downButtonImage.scaleToHeight(buttonSize)
        pauseButtonImage = pauseButtonImage.scaleToHeight(buttonSize)
        playButtonImage = playButtonImage.scaleToHeight(buttonSize)

        // Initialize Sprites
        upButton = GSprite(upButtonImage)
        leftButton = GSprite(leftButtonImage)
        rightButton = GSprite(rightButtonImage)
        downButton = GSprite(downButtonImage)
        pauseNPlayButton = GSprite(pauseButtonImage)
        if(startGameOnPause) pauseNPlayButton.bitmap = playButtonImage

        // Set X Coordinates
        val buttonGap = buttonSize * percentOfButtonsWidthForButtonsGap
        val middleButtonsX = (width - upButton.width) / 2
        upButton.x = middleButtonsX
        leftButton.x = (middleButtonsX - buttonSize - buttonGap).toFloat()
        rightButton.x = (middleButtonsX + buttonSize + buttonGap).toFloat()
        downButton.x = middleButtonsX
        pauseNPlayButton.x = middleButtonsX

        // Set Y Coordinates
        val totalHeightButtonsTake = buttonSize * 3 + buttonGap * 2
        upButton.y = (width + (remainingHeightBelowGrid - totalHeightButtonsTake) / 2).toFloat()
        pauseNPlayButton.y = (upButton.y + buttonSize + buttonGap).toFloat()
        leftButton.y = pauseNPlayButton.y
        rightButton.y = pauseNPlayButton.y
        downButton.y = (pauseNPlayButton.y + buttonSize + buttonGap).toFloat()

        // Add Buttons
        add(upButton)
        add(leftButton)
        add(rightButton)
        add(downButton)
        if(!hidePauseButton)
            add(pauseNPlayButton)
    }

    // required override function to activate GestureDetector
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(motionEvent)
        return true
    }

    // Gets the element clicked on by user
    override fun onSingleTapUp(motionEvent: MotionEvent?): Boolean {
        if(motionEvent == null) return false

        when(getElementAt(GPoint(motionEvent.x, motionEvent.y))) {
            upButton -> upButtonClicked()
            leftButton -> leftButtonClicked()
            rightButton -> rightButtonClicked()
            downButton -> downButtonClicked()
            pauseNPlayButton -> pauseNPlayButtonClicked()
        }

        return false
    }

    private fun upButtonClicked() {
        if(!movingDown && !gameIsPaused) {
            movingUp = true
            movingLeft = false
            movingRight = false
        }
    }

    private fun leftButtonClicked() {
        if(!movingRight && !gameIsPaused) {
            movingUp = false
            movingLeft = true
            movingDown = false
        }
    }

    private fun rightButtonClicked() {
        if(!movingLeft && !gameIsPaused) {
            movingUp = false
            movingRight = true
            movingDown = false
        }
    }

    private fun downButtonClicked() {
        if(!movingUp && !gameIsPaused) {
            movingLeft = false
            movingRight = false
            movingDown = true
        }
    }

    private fun pauseNPlayButtonClicked() {
        if(gameIsPaused) {
            gameIsPaused = false
            pauseNPlayButton.bitmap = pauseButtonImage
        } else {
            gameIsPaused = true
            pauseNPlayButton.bitmap = playButtonImage
        }
    }

    // required override functions to implement GestureDetector
    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }
    override fun onShowPress(e: MotionEvent?) {
        //
    }
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }
    override fun onLongPress(e: MotionEvent?) {
        //
    }
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }
}