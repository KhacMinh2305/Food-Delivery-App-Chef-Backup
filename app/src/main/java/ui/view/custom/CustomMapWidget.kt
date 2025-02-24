package ui.view.custom
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.deliveryfoodchef.R
import kotlin.math.roundToInt

class CustomMapWidget(private val context : Context, private val attrs : AttributeSet) : View(context, attrs) {

    enum class GestureType {
        MOVE, CLICK
    }

    // y = ax + b (the vertical orientation is opposite with the vertical display orientation)
    // So , the real Y coordinator to draw is : <height - y>
    class LineFunction(private val a : Float, private val b : Float) {
        fun findX(y : Float) = (y - b) / a

        fun findY(x : Float) = (a * x + b)

        override fun toString(): String {
            return "y = ${a}x + $b"
        }
    }

    companion object {
        private const val TOP_PADDING = 30
        private const val COLUMN_TEXT_AREA_HEIGHT = 60
        private const val COLUMN_TEXT_SIZE = 30f
        private const val DISPLAY_COLUMN_NUMBER = 7
        private const val INDICATOR_SIZE = 18f
        private const val SWIPE_VELOCITY = 0.8f
    }

    private val textPaint = Paint().apply {
        strokeWidth = 3f
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = COLUMN_TEXT_SIZE
        color = resources.getColor(R.color.map_text, null)
        isUnderlineText = true
        typeface = resources.getFont(R.font.poppins_semi_bold)
    }

    private val mapLinePaint = Paint().apply {
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = resources.getColor(R.color.orange, null)
    }

    private val indicatorPaint = Paint().apply {
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = resources.getColor(R.color.orange, null)
    }

    private val backgroundPaint = Paint()

    // constants
    // private var scaleRate = 1f // used for scaling map
    private var totalWidth = 0 // total width of view
    private var totalHeight = 0
    private var usedHeight = 0 // display map line part
    private var columnDistance = 0 // The distance between two columns
    private var listMoney = listOf( // values list for each column
        178.32, 423.98, 67.15, 295.41, 98.76, 451.09, 287.62, 125.83, 410.55, 379.21, 345.90, 163.74,
        558.18, 402.57, 81.33, 316.96, 465.29, 149.67, 253.84, 391.12, 159.48, 17.03, 334.75, 292.86
    )
    private lateinit var columnHeights : List<Int>
    private val lineFunctions = mutableListOf<LineFunction>()
    private val translateMatrix = Matrix().apply {
        preTranslate(40f, 0f)
    }
    private val indicatorTranslateMatrix = Matrix()
    private var translateRange = 0f
    private var currentX = 40f
    private var maxIndicatorTranslate = 0f
    private var indicatorTranX = 0f

    fun setMoney(money : List<Double>) { // uses to get money when using in programing
        if(money.isEmpty()) {
            Log.e("CustomMapWidget", "Empty List")
            return
        }
        listMoney = money
        invalidate()
    }

    private fun calculateSizes() {
        totalWidth = width
        totalHeight = height
        usedHeight = totalHeight - TOP_PADDING - COLUMN_TEXT_AREA_HEIGHT
        columnDistance = totalWidth / (DISPLAY_COLUMN_NUMBER - 1) // n columns generate (n - 1) distances between each two columns
        translateRange = ((listMoney.size - 1) * columnDistance - totalWidth).toFloat() + 38f
        maxIndicatorTranslate = (columnDistance * (listMoney.size - 1)).toFloat()
    }

    private fun calculateColumnHeightBaseOnMoney() : Boolean {
        if(listMoney.isEmpty()) return false
        val maxValue = listMoney.max()
        columnHeights = listMoney.map {
            (it / maxValue * usedHeight).roundToInt()
        }
        return true
    }

    private fun findFuncForTwoPoints(x1 : Float, y1 : Float, x2 : Float, y2 : Float) : LineFunction {
        val b = ((y1 * x2) - (y2 * x1)) / (x2 - x1)
        val a = (y1 - b) / x1
        return LineFunction(a, b)
    }

    private fun generatePointFunctions() {
        var firstX = 1f
        for(i in 0 until (columnHeights.size - 1)) {
            lineFunctions.add(findFuncForTwoPoints(firstX, columnHeights[i].toFloat(), firstX + columnDistance.toFloat(), columnHeights[i + 1].toFloat()))
            firstX += columnDistance
        }
    }

    private var initialized = false // calculate input one time. Only re-calculate when support scaling map

    private fun calculateIndicatorYOnTranslating(x : Float) : Float {
        val division = ((x / columnDistance).toInt()).coerceAtMost(lineFunctions.size - 1)
        val func = lineFunctions[division]
        return (usedHeight + TOP_PADDING) - func.findY(x)
    }

    //--------------------------------------------------------Generate shapes to draw--------------------------------------------------------

    private fun generateColumnText(i : Int, firstColumnLeft : Float) : Path {
        val textPath = Path()
        val text = if(i <= 12) "${i}AM" else "${i - 12}PM"
        textPaint.getTextPath(text, 0, text.length, firstColumnLeft, totalHeight.toFloat() - COLUMN_TEXT_AREA_HEIGHT.toFloat() * 1 / 4, textPath)
        textPath.transform(translateMatrix)
        return textPath
    }

    private fun generateMapLines() : Path {
        val calHeight = usedHeight + TOP_PADDING
        var pointX = 0f
        val line = Path().apply {
            moveTo(0f, calHeight - columnHeights[0].toFloat())
        }
        pointX += columnDistance
        for(i in 1 until columnHeights.size) {
            val pointY = calHeight - columnHeights[i].toFloat()
            line.lineTo(pointX, pointY)
            pointX += columnDistance
        }
        line.apply {
            transform(translateMatrix)
        }
        return line
    }

    private fun generateIndicatorOuterCircle() : Path {
        indicatorPaint.apply {
            shader = null
            strokeWidth = 6f
            style = Paint.Style.STROKE
            color = resources.getColor(R.color.orange, null)
        }
        val indicatorTranY = calculateIndicatorYOnTranslating(indicatorTranX)
        val indicatorPath = Path().apply {
            // create circle outline
            addCircle(-3f, indicatorTranY - INDICATOR_SIZE / 2 + 3f, INDICATOR_SIZE, Path.Direction.CW)
            val replacedMatrix = Matrix().apply {
                set(translateMatrix)
            }
            transform(replacedMatrix.apply {
                preConcat(indicatorTranslateMatrix)
            })
        }
        return indicatorPath
    }

    private fun generateIndicatorInnerCircle() : Path {
        indicatorPaint.apply {
            shader = null
            style = Paint.Style.FILL
            color = resources.getColor(R.color.white, null)
        }
        return Path().apply {
            val indicatorTranY = calculateIndicatorYOnTranslating(indicatorTranX)
            addCircle(-3f, indicatorTranY - INDICATOR_SIZE / 2 + 3f, INDICATOR_SIZE - 3f, Path.Direction.CW)
            val replacedMatrix = Matrix().apply {
                set(translateMatrix)
            }
            transform(replacedMatrix.apply {
                preConcat(indicatorTranslateMatrix)
            })
        }
    }

    private fun generateIndicatorLine() : Path {
        val indicatorTranY = calculateIndicatorYOnTranslating(indicatorTranX)
        val linePath =  Path().apply {
            moveTo(-2f, indicatorTranY + 10f)
            lineTo(-2f, (totalHeight - COLUMN_TEXT_AREA_HEIGHT).toFloat())
            val replacedMatrix = Matrix().apply {
                set(translateMatrix)
            }
            transform(replacedMatrix.apply {
                preConcat(indicatorTranslateMatrix)
            })
        }
        indicatorPaint.apply {
            val gradient = LinearGradient(
                -2f, indicatorTranY + 10f,
                -2f, (totalHeight - COLUMN_TEXT_AREA_HEIGHT).toFloat(),
                resources.getColor(R.color.orange, null), resources.getColor(R.color.transparent, null),
                Shader.TileMode.CLAMP)
            shader = gradient
            style = Paint.Style.STROKE
            strokeWidth = 8f
            color = resources.getColor(R.color.orange, null)
        }
        return linePath
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(!initialized) {
            calculateSizes()
            calculateColumnHeightBaseOnMoney()
            generatePointFunctions()
            backgroundPaint.apply {
                val gradient = LinearGradient(
                    columnDistance * (listMoney.size - 1) / 2f, 0f,
                    columnDistance * (listMoney.size - 1) / 2f, totalHeight.toFloat() * 2 / 3,
                    resources.getColor(R.color.light_orange, null), resources.getColor(R.color.transparent, null),
                    Shader.TileMode.CLAMP)
                shader = gradient
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            initialized = true
        }

        // TODO : draw column text
        var firstColumnLeft = 0f
        textPaint.textAlign = Paint.Align.CENTER
        for(i in listMoney.indices) {
            canvas.drawPath(generateColumnText(i, firstColumnLeft), textPaint)
            firstColumnLeft += columnDistance
        }

        // TODO : draw line
        val mapLine = generateMapLines()
        canvas.drawPath(mapLine, mapLinePaint)

        // TODO : draw gradient background
        val backgroundLine = Path().apply {
            set(mapLine)
            lineTo(columnDistance * (columnHeights.size - 1).toFloat(), totalHeight.toFloat())
            lineTo(0f, totalHeight.toFloat())
            lineTo(0f, usedHeight + TOP_PADDING - columnHeights[0].toFloat())
            close()
        }
        canvas.drawPath(backgroundLine, backgroundPaint)

        // TODO : draw indicator
        canvas.drawPath(generateIndicatorOuterCircle(), indicatorPaint)
        canvas.drawPath(generateIndicatorInnerCircle(), indicatorPaint)
        canvas.drawPath(generateIndicatorLine(), indicatorPaint)
    }

    //-----------------------------------------------------Gesture Handling-----------------------------------------------------

    fun onUserSwipe(dx : Int, dy : Int, translateX : Int) {
        val moveIndicator = dy < totalHeight - COLUMN_TEXT_AREA_HEIGHT
        if(moveIndicator) onMoveIndicator(translateX) else onTranslateMap(translateX)
    }

    private fun onMoveIndicator(translateX : Int) {
        // check if user attempt to swipe indicator out of range
        if(indicatorTranX >= maxIndicatorTranslate && translateX >= 0) {  // drag indicator to right (near to max)
            indicatorTranX = maxIndicatorTranslate
            indicatorTranslateMatrix.apply {
                reset()
                preTranslate(indicatorTranX, 0f)
            }
        } else if(indicatorTranX <= 0f && translateX <= 0) { // drag indicator to left (near to 0)
            indicatorTranX = 0f
            indicatorTranslateMatrix.apply {
                reset()
                preTranslate(indicatorTranX, 0f)
            }
        } else { // maintain the current coordinator X of indicator
            val moveX = translateX * SWIPE_VELOCITY
            indicatorTranX += moveX
            indicatorTranslateMatrix.apply {
                preTranslate(moveX, 0f)
            }
        }
        postInvalidate()
    }

    private fun onTranslateMap(translateX : Int) {
        // prevent user to swipe out of range
        if(currentX >= 40f && translateX >= 0) {
            currentX = 40f
            translateMatrix.apply {
                reset()
                preTranslate(currentX, 0f)
            }
            return
        } else if(currentX <= -translateRange && translateX <= 0) {
            currentX = -translateRange
            translateMatrix.apply {
                reset()
                preTranslate(currentX, 0f)
            }
            return
        }
        currentX += translateX
        // apply translate matrix to update on swiping
        translateMatrix.apply {
            preTranslate(+translateX.toFloat(), 0f)
        }
        postInvalidate()
    }

    fun onTouchEventEnd(eventType : GestureType) {
        if(eventType == GestureType.CLICK) {
            // handle click
            return
        }
        // handle move
    }

    private fun anchorIndicatorToNearestColumn() {
        val division = indicatorTranX / columnDistance
        val remainder = indicatorTranX % columnDistance
        val lineIndex = division + remainder / (columnDistance / 2)
    }

    // Tasks :
    // 1. calculate sizes (Done)
    // 2. calculate line functions (Done)
    // 3. draw column text (Done)
    // 4. draw line (Done)
    // 5. draw gradient background for line (Done)
    // 7. draw indicator (Done)
    // 8. Add tracker whether user click to (if map part => move indicator, if text part => move map) (Done)
    // 9. process moving map view (Done)
    // 10. process moving indicator point (Done)
}