package sticky.header.tableview.stickyheadertableview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.MotionEvent
import androidx.annotation.ColorInt
import androidx.core.view.NestedScrollingChildHelper
import sticky.header.tableview.R

class StickyHeaderTableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BiDirectionalScrollableView(context, attrs, defStyleAttr) {

    override val nestedScrollingChildHelper = NestedScrollingChildHelper(this)

    //region Configurable variables via xml or setter methods
    /**
     * @return data which is previously set by setData(data) method. otherwise null.
     */
    var data: List<List<String>>? = null
        set(data) {
            field = data
            cellsRectangles = if (data != null) {
                Array(data.size) {
                    arrayOfNulls(data[0].size)
                }
            } else {
                arrayOf()
            }
            updateLayoutChanges()
        }
    private var isDisplayLeftHeadersVertically = false
    private var isWrapHeightOfEachRow = false
    private var isWrapWidthOfEachColumn = false
    private var textLabelColor = 0
    private var textHeaderColor = 0
    private var dividerColor = 0
    private var textLabelSize = 0
    private var textHeaderSize = 0
    private var dividerThickness = 0
    private var headerCellFillColor = 0
    private var contentCellFillColor = 0
    private var cellPadding = 0
    //endregion Configurable variables via xml or setter methods

    //region Variables for drawing
    private val paintStrokeRect = Paint()
    private val paintHeaderCellFillRect = Paint()
    private val paintContentCellFillRect = Paint()
    private val paintLabelText = Paint()
    private val paintHeaderText = Paint()
    private val textRectBounds = Rect()

    /**
     * Used to draw cells on canvas and identify clicked position for #OnTableCellClickListener
     */
    private var cellsRectangles = arrayOf<Array<Rect?>>()
    private var maxWidthOfCell = 0
    private var maxHeightOfCell = 0
    private var maxHeightSparseIntArray = SparseIntArray()
    private var maxWidthSparseIntArray = SparseIntArray()
    //endregion Variables for drawing

    //region Constructor and setup methods
    init {
        val displayMatrixHelper = DisplayMatrixHelper()
        val defaultTextSize = displayMatrixHelper.dpToPixels(getContext(), 14f).toInt()
        val a = context.theme.obtainStyledAttributes(
            attrs, R.styleable.StickyHeaderTableView, defStyleAttr, defStyleAttr
        )
        try {
            textLabelColor = a.getColor(
                R.styleable.StickyHeaderTableView_shtv_textLabelColor, Color.BLACK
            )
            textHeaderColor = a.getColor(
                R.styleable.StickyHeaderTableView_shtv_textHeaderColor, Color.BLACK
            )
            dividerColor = a.getColor(
                R.styleable.StickyHeaderTableView_shtv_dividerColor, Color.BLACK
            )
            textLabelSize = a.getDimensionPixelSize(
                R.styleable.StickyHeaderTableView_shtv_textLabelSize, defaultTextSize
            )
            textHeaderSize = a.getDimensionPixelSize(
                R.styleable.StickyHeaderTableView_shtv_textHeaderSize, defaultTextSize
            )
            dividerThickness = a.getDimensionPixelSize(
                R.styleable.StickyHeaderTableView_shtv_dividerThickness,
                0
            )
            cellPadding =
                a.getDimensionPixelSize(R.styleable.StickyHeaderTableView_shtv_cellPadding, 0)
            isDisplayLeftHeadersVertically = a.getBoolean(
                R.styleable.StickyHeaderTableView_shtv_isDisplayLeftHeadersVertically,
                false
            )
            isWrapHeightOfEachRow = a.getBoolean(
                R.styleable.StickyHeaderTableView_shtv_isWrapHeightOfEachRow,
                false
            )
            isWrapWidthOfEachColumn = a.getBoolean(
                R.styleable.StickyHeaderTableView_shtv_isWrapWidthOfEachColumn,
                false
            )
            headerCellFillColor = a.getColor(
                R.styleable.StickyHeaderTableView_shtv_headerCellFillColor, Color.TRANSPARENT
            )
            contentCellFillColor = a.getColor(
                R.styleable.StickyHeaderTableView_shtv_contentCellFillColor, Color.TRANSPARENT
            )
        } catch (e: Exception) {
            setupDefaultVariableValue(defaultTextSize)
        } finally {
            a.recycle()
        }
        setupPaint()
    }

    private fun setupDefaultVariableValue(defaultTextSize: Int) {
        textLabelColor = Color.BLACK
        textHeaderColor = Color.BLACK
        dividerColor = Color.BLACK
        textLabelSize = defaultTextSize
        textHeaderSize = defaultTextSize
        dividerThickness = 0
        cellPadding = 0
        headerCellFillColor = Color.TRANSPARENT
        contentCellFillColor = Color.TRANSPARENT
    }

    private fun setupPaint() {
        paintStrokeRect.style = Paint.Style.STROKE
        paintStrokeRect.color = dividerColor
        paintStrokeRect.strokeWidth = dividerThickness.toFloat()
        paintHeaderCellFillRect.style = Paint.Style.FILL
        paintHeaderCellFillRect.color = headerCellFillColor
        paintContentCellFillRect.style = Paint.Style.FILL
        paintContentCellFillRect.color = contentCellFillColor
        paintLabelText.style = Paint.Style.FILL
        paintLabelText.color = textLabelColor
        paintLabelText.textSize = textLabelSize.toFloat()
        paintLabelText.textAlign = Paint.Align.LEFT
        paintHeaderText.style = Paint.Style.FILL
        paintHeaderText.color = textHeaderColor
        paintHeaderText.textSize = textHeaderSize.toFloat()
        paintHeaderText.textAlign = Paint.Align.LEFT
    }
    //endregion Constructor and setup methods

    //region Measure/Update the view
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var desiredWidth = 0
        var desiredHeight = 0
        if (data != null) {
            updateMaxWidthHeightOfCell()
            if (isWrapHeightOfEachRow) {
                for (i in 0 until maxHeightSparseIntArray.size()) {
                    desiredHeight += maxHeightSparseIntArray[i, 0]
                }
                desiredHeight += dividerThickness / 2
            } else {
                desiredHeight = maxHeightOfCell * data!!.size + dividerThickness / 2
            }
            if (isWrapWidthOfEachColumn) {
                for (i in 0 until maxWidthSparseIntArray.size()) {
                    desiredWidth += maxWidthSparseIntArray[i, 0]
                }
                desiredWidth += dividerThickness / 2
            } else {
                desiredWidth = maxWidthOfCell * data!![0].size + dividerThickness / 2
            }
        }
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //Measure Width
        val width = when (widthMode) {
            //Must be this size
            MeasureSpec.EXACTLY -> widthSize
            //Can't be bigger than...
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(widthSize)
            //Be whatever you want
            else -> desiredWidth
        }

        //Measure Height
        val height = when (heightMode) {
            //Must be this size
            MeasureSpec.EXACTLY -> heightSize
            //Can't be bigger than...
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            //Be whatever you want
            else -> desiredHeight
        }

        //MUST CALL THIS both methods
        setDesiredDimension(desiredWidth, desiredHeight)
        setMeasuredDimension(width, height)
    }

    /**
     * Calculate and update max width height of cell<br></br>
     * Required for onMeasure() method
     */
    private fun updateMaxWidthHeightOfCell() {
        maxWidthOfCell = 0
        maxHeightOfCell = 0
        maxHeightSparseIntArray = SparseIntArray()
        maxWidthSparseIntArray = SparseIntArray()
        val doubleCellPadding = cellPadding + cellPadding
        for (i in data!!.indices) {
            for (j in data!![0].indices) {
                if (i == 0 && j == 0) {
                    paintHeaderText.getTextBounds(
                        data!![i][j], 0, data!![i][j].length, textRectBounds
                    )
                    if (maxWidthOfCell < textRectBounds.width()) {
                        maxWidthOfCell = textRectBounds.width()
                    }
                    if (maxHeightOfCell < textRectBounds.height()) {
                        maxHeightOfCell = textRectBounds.height()
                    }
                    if (maxWidthSparseIntArray[j, 0] < textRectBounds.width()) {
                        maxWidthSparseIntArray.put(j, textRectBounds.width())
                    }
                    if (maxHeightSparseIntArray[i, 0] < textRectBounds.height()) {
                        maxHeightSparseIntArray.put(i, textRectBounds.height())
                    }
                } else if (i == 0) {
                    // Top headers cells
                    paintHeaderText.getTextBounds(
                        data!![i][j], 0, data!![i][j].length, textRectBounds
                    )
                    if (maxWidthOfCell < textRectBounds.width()) {
                        maxWidthOfCell = textRectBounds.width()
                    }
                    if (maxHeightOfCell < textRectBounds.height()) {
                        maxHeightOfCell = textRectBounds.height()
                    }
                    if (maxWidthSparseIntArray[j, 0] < textRectBounds.width()) {
                        maxWidthSparseIntArray.put(j, textRectBounds.width())
                    }
                    if (maxHeightSparseIntArray[i, 0] < textRectBounds.height()) {
                        maxHeightSparseIntArray.put(i, textRectBounds.height())
                    }
                } else if (j == 0) {
                    // Left headers cells
                    paintHeaderText.getTextBounds(
                        data!![i][j], 0, data!![i][j].length, textRectBounds
                    )
                    if (isDisplayLeftHeadersVertically) {
                        if (maxWidthOfCell < textRectBounds.height()) {
                            maxWidthOfCell = textRectBounds.height()
                        }
                        if (maxHeightOfCell < textRectBounds.width()) {
                            maxHeightOfCell = textRectBounds.width()
                        }
                        if (maxWidthSparseIntArray[j, 0] < textRectBounds.height()) {
                            maxWidthSparseIntArray.put(j, textRectBounds.height())
                        }
                        if (maxHeightSparseIntArray[i, 0] < textRectBounds.width()) {
                            maxHeightSparseIntArray.put(i, textRectBounds.width())
                        }
                    } else {
                        if (maxWidthOfCell < textRectBounds.width()) {
                            maxWidthOfCell = textRectBounds.width()
                        }
                        if (maxHeightOfCell < textRectBounds.height()) {
                            maxHeightOfCell = textRectBounds.height()
                        }
                        if (maxWidthSparseIntArray[j, 0] < textRectBounds.width()) {
                            maxWidthSparseIntArray.put(j, textRectBounds.width())
                        }
                        if (maxHeightSparseIntArray[i, 0] < textRectBounds.height()) {
                            maxHeightSparseIntArray.put(i, textRectBounds.height())
                        }
                    }
                } else {
                    // Other content cells
                    paintLabelText.getTextBounds(
                        data!![i][j], 0, data!![i][j].length, textRectBounds
                    )
                    if (maxWidthOfCell < textRectBounds.width()) {
                        maxWidthOfCell = textRectBounds.width()
                    }
                    if (maxHeightOfCell < textRectBounds.height()) {
                        maxHeightOfCell = textRectBounds.height()
                    }
                    if (maxWidthSparseIntArray[j, 0] < textRectBounds.width()) {
                        maxWidthSparseIntArray.put(j, textRectBounds.width())
                    }
                    if (maxHeightSparseIntArray[i, 0] < textRectBounds.height()) {
                        maxHeightSparseIntArray.put(i, textRectBounds.height())
                    }
                }
            }
        }
        maxWidthOfCell += doubleCellPadding
        maxHeightOfCell += doubleCellPadding
        for (i in 0 until maxHeightSparseIntArray.size()) {
            maxHeightSparseIntArray.put(i, maxHeightSparseIntArray[i, 0] + doubleCellPadding)
        }
        for (i in 0 until maxWidthSparseIntArray.size()) {
            maxWidthSparseIntArray.put(i, maxWidthSparseIntArray[i, 0] + doubleCellPadding)
        }
    }

    private fun updateLayoutChanges() {
        invalidate()
        requestLayout()
        /*if (!isInLayout) {
            requestLayout()
        }*/
    }
    //endregion Measure/Update the view

    //region Canvas Drawing
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data == null) {
            return
        }

        //region Variables
        var cellLeftX: Int
        var cellTopY = scrolledContentRect.top
        var cellRightX: Int
        var cellBottomY = scrolledContentRect.top + getHeightOfRow(0)
        val halfDividerThickness = dividerThickness / 2
        var drawTextX: Float
        var drawTextY: Float
        var textToDraw: String
        //endregion Variables

        //region Update each cell rectangle as per scrolledRect

        // This is top-left most cell (0,0)
        updateCellRectangle(
            0,
            0,
            halfDividerThickness,
            halfDividerThickness,
            getWidthOfColumn(0),
            getHeightOfRow(0)
        )
        for (i in data!!.indices) {
            cellRightX = scrolledContentRect.left
            val heightOfRowI = getHeightOfRow(i)
            if (i == 0) {
                cellTopY = halfDividerThickness
                for (j in data!![i].indices) {
                    cellLeftX = cellRightX - halfDividerThickness
                    cellRightX += getWidthOfColumn(j)
                    if (j != 0) {
                        // This are top header cells (0,*)
                        updateCellRectangle(i, j, cellLeftX, cellTopY, cellRightX, heightOfRowI)
                    }
                }
                cellBottomY = scrolledContentRect.top + getHeightOfRow(i)
            } else {
                // These are content cells
                for (j in data!![0].indices) {
                    cellLeftX = cellRightX - halfDividerThickness
                    cellRightX += getWidthOfColumn(j)
                    if (j != 0) {
                        updateCellRectangle(i, j, cellLeftX, cellTopY, cellRightX, cellBottomY)
                    }
                }

                // This are left header cells (*,0)
                cellRightX = 0
                cellLeftX = cellRightX + halfDividerThickness
                cellRightX += getWidthOfColumn(0)
                updateCellRectangle(i, 0, cellLeftX, cellTopY, cellRightX, cellBottomY)
            }
            cellTopY = cellBottomY - halfDividerThickness
            cellBottomY += getHeightOfRow(i + 1)
        }

        //endregion Update each cell rectangle as per scrolledRect

        //region Draw contents & left headers
        var isLeftVisible: Boolean
        var isTopVisible: Boolean
        var isRightVisible: Boolean
        var isBottomVisible: Boolean
        for (i in 1 until data!!.size) {
            isTopVisible = (cellsRectangles[i][0]!!.top >= cellsRectangles[0][0]!!.bottom
                && cellsRectangles[i][0]!!.top <= viewRect.bottom)
            isBottomVisible = (cellsRectangles[i][0]!!.bottom >= cellsRectangles[0][0]!!.bottom
                && cellsRectangles[i][0]!!.bottom <= viewRect.bottom)
            if (isTopVisible || isBottomVisible) {

                // Draw contents
                for (j in 1 until data!![i].size) {
                    isLeftVisible = (cellsRectangles[i][j]!!.left >= cellsRectangles[i][0]!!.right
                        && cellsRectangles[i][j]!!.left <= viewRect.right)
                    isRightVisible = (cellsRectangles[i][j]!!.right >= cellsRectangles[i][0]!!.right
                        && cellsRectangles[i][j]!!.right <= viewRect.right)
                    if (isLeftVisible || isRightVisible) {
                        canvas.drawRect(
                            cellsRectangles[i][j]!!.left.toFloat(),
                            cellsRectangles[i][j]!!.top.toFloat(),
                            cellsRectangles[i][j]!!.right.toFloat(),
                            cellsRectangles[i][j]!!.bottom.toFloat(),
                            paintContentCellFillRect
                        )
                        if (dividerThickness != 0) {
                            canvas.drawRect(
                                cellsRectangles[i][j]!!.left.toFloat(),
                                cellsRectangles[i][j]!!.top.toFloat(),
                                cellsRectangles[i][j]!!.right.toFloat(),
                                cellsRectangles[i][j]!!.bottom.toFloat(),
                                paintStrokeRect
                            )
                        }
                        textToDraw = data!![i][j]
                        paintLabelText.getTextBounds(
                            textToDraw, 0, textToDraw.length, textRectBounds
                        )
                        drawTextX =
                            cellsRectangles[i][j]!!.right - getWidthOfColumn(j) / 2f - textRectBounds.width() / 2f
                        drawTextY =
                            cellsRectangles[i][j]!!.bottom - getHeightOfRow(i) / 2f + textRectBounds.height() / 2f
                        canvas.drawText(
                            textToDraw, 0, textToDraw.length, drawTextX, drawTextY, paintLabelText
                        )
                    }
                }

                // Draw left header (*,0)
                canvas.drawRect(
                    cellsRectangles[i][0]!!.left.toFloat(),
                    cellsRectangles[i][0]!!.top.toFloat(),
                    cellsRectangles[i][0]!!.right.toFloat(),
                    cellsRectangles[i][0]!!.bottom.toFloat(),
                    paintHeaderCellFillRect
                )
                if (dividerThickness != 0) {
                    canvas.drawRect(
                        cellsRectangles[i][0]!!.left.toFloat(),
                        cellsRectangles[i][0]!!.top.toFloat(),
                        cellsRectangles[i][0]!!.right.toFloat(),
                        cellsRectangles[i][0]!!.bottom.toFloat(),
                        paintStrokeRect
                    )
                }
                textToDraw = data!![i][0]
                paintHeaderText.getTextBounds(textToDraw, 0, textToDraw.length, textRectBounds)
                if (isDisplayLeftHeadersVertically) {
                    drawTextX =
                        cellsRectangles[i][0]!!.right - getWidthOfColumn(0) / 2f + textRectBounds.height() / 2f
                    drawTextY =
                        cellsRectangles[i][0]!!.bottom - getHeightOfRow(i) / 2f + textRectBounds.width() / 2f
                    canvas.save()
                    canvas.rotate(-90f, drawTextX, drawTextY)
                    canvas.drawText(
                        textToDraw, 0, textToDraw.length, drawTextX, drawTextY, paintHeaderText
                    )
                    canvas.restore()
                } else {
                    drawTextX =
                        cellsRectangles[i][0]!!.right - getWidthOfColumn(0) / 2f - textRectBounds.width() / 2f
                    drawTextY =
                        cellsRectangles[i][0]!!.bottom - getHeightOfRow(i) / 2f + textRectBounds.height() / 2f
                    canvas.drawText(
                        textToDraw, 0, textToDraw.length, drawTextX, drawTextY, paintHeaderText
                    )
                }
            }
        }

        //endregion Draw contents & left headers

        //region Draw top headers (0,*)
        for (j in 1 until data!![0].size) {
            isLeftVisible = (cellsRectangles[0][j]!!.left >= cellsRectangles[0][0]!!.right
                && cellsRectangles[0][j]!!.left <= viewRect.right)
            isRightVisible = (cellsRectangles[0][j]!!.right >= cellsRectangles[0][0]!!.right
                && cellsRectangles[0][j]!!.right <= viewRect.right)
            if (isLeftVisible || isRightVisible) {
                canvas.drawRect(
                    cellsRectangles[0][j]!!.left.toFloat(),
                    cellsRectangles[0][j]!!.top.toFloat(),
                    cellsRectangles[0][j]!!.right.toFloat(),
                    cellsRectangles[0][j]!!.bottom.toFloat(),
                    paintHeaderCellFillRect
                )
                if (dividerThickness != 0) {
                    canvas.drawRect(
                        cellsRectangles[0][j]!!.left.toFloat(),
                        cellsRectangles[0][j]!!.top.toFloat(),
                        cellsRectangles[0][j]!!.right.toFloat(),
                        cellsRectangles[0][j]!!.bottom.toFloat(),
                        paintStrokeRect
                    )
                }
                textToDraw = data!![0][j]
                paintHeaderText.getTextBounds(textToDraw, 0, textToDraw.length, textRectBounds)
                drawTextX =
                    cellsRectangles[0][j]!!.right - getWidthOfColumn(j) / 2f - textRectBounds.width() / 2f
                drawTextY =
                    cellsRectangles[0][j]!!.bottom - getHeightOfRow(0) / 2f + textRectBounds.height() / 2f
                canvas.drawText(
                    textToDraw, 0, textToDraw.length, drawTextX, drawTextY, paintHeaderText
                )
            }
        }

        //endregion Draw top headers (0,*)

        //region Draw top-left most cell (0,0)
        canvas.drawRect(
            cellsRectangles[0][0]!!.left.toFloat(),
            cellsRectangles[0][0]!!.top.toFloat(),
            cellsRectangles[0][0]!!.right.toFloat(),
            cellsRectangles[0][0]!!.bottom.toFloat(),
            paintHeaderCellFillRect
        )
        if (dividerThickness != 0) {
            canvas.drawRect(
                cellsRectangles[0][0]!!.left.toFloat(),
                cellsRectangles[0][0]!!.top.toFloat(),
                cellsRectangles[0][0]!!.right.toFloat(),
                cellsRectangles[0][0]!!.bottom.toFloat(),
                paintStrokeRect
            )
        }
        textToDraw = data!![0][0]
        paintHeaderText.getTextBounds(textToDraw, 0, textToDraw.length, textRectBounds)
        drawTextX = getWidthOfColumn(0) - getWidthOfColumn(0) / 2f - textRectBounds.width() / 2f
        drawTextY = getHeightOfRow(0) - getHeightOfRow(0) / 2f + textRectBounds.height() / 2f
        canvas.drawText(textToDraw, 0, textToDraw.length, drawTextX, drawTextY, paintHeaderText)

        //endregion Draw top-left most cell (0,0)

        //region Draw whole view border
        if (dividerThickness != 0) {
            canvas.drawRect(
                viewRect.left.toFloat(),
                viewRect.top.toFloat(),
                (viewRect.right - halfDividerThickness).toFloat(),
                (viewRect.bottom - halfDividerThickness).toFloat(),
                paintStrokeRect
            )
        }
        //endregion Draw whole view border
    }

    /**
     * This will update cell bound rect data, which is used for handling cell click event
     *
     * @param i           row position
     * @param j           column position
     * @param cellLeftX   leftX
     * @param cellTopY    topY
     * @param cellRightX  rightX
     * @param cellBottomY bottomY
     */
    private fun updateCellRectangle(
        i: Int, j: Int, cellLeftX: Int, cellTopY: Int, cellRightX: Int, cellBottomY: Int
    ) {
        if (cellsRectangles[i][j] == null) {
            cellsRectangles[i][j] = Rect(cellLeftX, cellTopY, cellRightX, cellBottomY)
        } else {
            cellsRectangles[i][j]!!.left = cellLeftX
            cellsRectangles[i][j]!!.top = cellTopY
            cellsRectangles[i][j]!!.right = cellRightX
            cellsRectangles[i][j]!!.bottom = cellBottomY
        }
    }

    private fun getWidthOfColumn(key: Int): Int {
        return if (isWrapWidthOfEachColumn) {
            maxWidthSparseIntArray[key, 0]
        } else {
            maxWidthOfCell
        }
    }

    private fun getHeightOfRow(key: Int): Int {
        return if (isWrapHeightOfEachRow) {
            maxHeightSparseIntArray[key, 0]
        } else {
            maxHeightOfCell
        }
    }
    //endregion Canvas Drawing

    /**
     * Set the table cell click listener
     */
    var onTableCellClickListener: OnTableCellClickListener? = null
    override fun onSingleTapUpEvent(e: MotionEvent) {
        if (onTableCellClickListener != null) {
            val x = e.x
            val y = e.y
            var isEndLoop = false
            for (i in cellsRectangles.indices) {
                if (cellsRectangles[i][0]!!.top <= y && cellsRectangles[i][0]!!.bottom >= y) {
                    for (j in 0 until cellsRectangles[0].size) {
                        if (cellsRectangles[i][j]!!.left <= x && cellsRectangles[i][j]!!.right >= x) {
                            isEndLoop = true
                            onTableCellClickListener!!.onTableCellClicked(i, j)
                            break
                        }
                    }
                }
                if (isEndLoop) {
                    break
                }
            }
        }
    }

    //region Getter/Setter methods
    /**
     * @return text color of the content cells
     */
    fun getTextLabelColor(): Int {
        return textLabelColor
    }

    /**
     * Set text color  for content cells
     *
     * @param textLabelColor color
     */
    fun setTextLabelColor(@ColorInt textLabelColor: Int) {
        this.textLabelColor = textLabelColor
        invalidate()
    }

    /**
     * @return text color of the header cells
     */
    fun getTextHeaderColor(): Int {
        return textHeaderColor
    }

    /**
     * Set text color  for header cells
     *
     * @param textHeaderColor color
     */
    fun setTextHeaderColor(@ColorInt textHeaderColor: Int) {
        this.textHeaderColor = textHeaderColor
        invalidate()
    }

    /**
     * @return color of the cell divider or cell border
     */
    fun getDividerColor(): Int {
        return dividerColor
    }

    /**
     * Set divider or border color  for cell
     *
     * @param dividerColor color
     */
    fun setDividerColor(@ColorInt dividerColor: Int) {
        this.dividerColor = dividerColor
        invalidate()
    }

    /**
     * @return text size in pixels of content cells
     */
    fun getTextLabelSize(): Int {
        return textLabelSize
    }

    /**
     * Set text size in pixels for content cells<br></br>
     * You can use [DisplayMatrixHelper.dpToPixels] method to convert dp to pixel
     *
     * @param textLabelSize text label size in pixels
     */
    fun setTextLabelSize(textLabelSize: Int) {
        this.textLabelSize = textLabelSize
        updateLayoutChanges()
    }

    /**
     * @return text header size in pixels of header cells
     */
    fun getTextHeaderSize(): Int {
        return textHeaderSize
    }

    /**
     * Set text header size in pixels for header cells<br></br>
     * You can use [DisplayMatrixHelper.dpToPixels] method to convert dp to pixel
     *
     * @param textHeaderSize text header size in pixels
     */
    fun setTextHeaderSize(textHeaderSize: Int) {
        this.textHeaderSize = textHeaderSize
        updateLayoutChanges()
    }

    /**
     * @return divider thickness in pixels
     */
    fun getDividerThickness(): Int {
        return dividerThickness
    }

    /**
     * Set divider thickness size in pixels for all cells<br></br>
     * You can use [DisplayMatrixHelper.dpToPixels] method to convert dp to pixel
     *
     * @param dividerThickness divider thickness size in pixels
     */
    fun setDividerThickness(dividerThickness: Int) {
        this.dividerThickness = dividerThickness
        invalidate()
    }

    /**
     * @return header cell's fill color
     */
    fun getHeaderCellFillColor(): Int {
        return headerCellFillColor
    }

    /**
     * Set header cell fill color
     *
     * @param headerCellFillColor color to fill in header cell
     */
    fun setHeaderCellFillColor(@ColorInt headerCellFillColor: Int) {
        this.headerCellFillColor = headerCellFillColor
        invalidate()
    }

    /**
     * @return content cell's fill color
     */
    fun getContentCellFillColor(): Int {
        return contentCellFillColor
    }

    /**
     * Set content cell fill color
     *
     * @param contentCellFillColor color to fill in content cell
     */
    fun setContentCellFillColor(@ColorInt contentCellFillColor: Int) {
        this.contentCellFillColor = contentCellFillColor
        invalidate()
    }

    /**
     * @return cell padding in pixels
     */
    fun getCellPadding(): Int {
        return cellPadding
    }

    /**
     * Set padding for all cell of table<br></br>
     * You can use [DisplayMatrixHelper.dpToPixels] method to convert dp to pixel
     *
     * @param cellPadding cell padding in pixels
     */
    fun setCellPadding(cellPadding: Int) {
        this.cellPadding = cellPadding
        updateLayoutChanges()
    }

    /**
     * @return true if left header cell text are displayed vertically enabled
     */
    fun isDisplayLeftHeadersVertically(): Boolean {
        return isDisplayLeftHeadersVertically
    }

    /**
     * Set left header text display vertically or horizontal
     *
     * @param displayLeftHeadersVertically true if you wants to set left header text display vertically
     */
    fun setDisplayLeftHeadersVertically(displayLeftHeadersVertically: Boolean) {
        isDisplayLeftHeadersVertically = displayLeftHeadersVertically
        updateLayoutChanges()
    }

    /**
     * @return true if you settled true for wrap height of each row
     */
    fun isWrapHeightOfEachRow(): Boolean {
        return isWrapHeightOfEachRow
    }

    /**
     * Set whether height of each row should wrap or not
     *
     * @param wrapHeightOfEachRow pass true if you wants to set each row should wrap the height
     */
    fun setWrapHeightOfEachRow(wrapHeightOfEachRow: Boolean) {
        isWrapHeightOfEachRow = wrapHeightOfEachRow
        updateLayoutChanges()
    }

    /**
     * @return true if you settled true for wrap width of each column
     */
    fun isWrapWidthOfEachColumn(): Boolean {
        return isWrapWidthOfEachColumn
    }

    /**
     * Set whether width of each column should wrap or not
     *
     * @param wrapWidthOfEachColumn pass true if you wants to set each column should wrap the width
     */
    fun setWrapWidthOfEachColumn(wrapWidthOfEachColumn: Boolean) {
        isWrapWidthOfEachColumn = wrapWidthOfEachColumn
        updateLayoutChanges()
    }
    //endregion Getter/Setter methods
}