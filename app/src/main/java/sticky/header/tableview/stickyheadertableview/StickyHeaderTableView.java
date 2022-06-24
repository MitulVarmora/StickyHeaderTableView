package sticky.header.tableview.stickyheadertableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseIntArray;

import androidx.annotation.Nullable;

import java.util.List;

import sticky.header.tableview.R;

/**
 * StickyHeaderTableView
 */
public class StickyHeaderTableView extends BiDirectionalScrollableView {

    //region Configurable variables via xml or setter methods
    private List<List<String>> data = null;
    private boolean isDisplayLeftHeadersVertically = false;
    private boolean isWrapHeightOfEachRow = false;
    private boolean isWrapWidthOfEachColumn = false;
    private int textLabelColor;
    private int textHeaderColor;
    private int dividerColor;
    private int textLabelSize;
    private int textHeaderSize;
    private int dividerThickness;
    private int headerCellFillColor;
    private int contentCellFillColor;
    private int cellPadding;
    //endregion Configurable variables via xml or setter methods

    //region Variables for drawing

    private final Paint paintStrokeRect = new Paint();
    private final Paint paintHeaderCellFillRect = new Paint();
    private final Paint paintContentCellFillRect = new Paint();
    private final Paint paintLabelText = new Paint();
    private final Paint paintHeaderText = new Paint();
    private final Rect textRectBounds = new Rect();

    /**
     * Used to draw cells on canvas and identify clicked position for #OnTableCellClickListener
     */
    private Rect[][] cellsRectangles = new Rect[][]{};
    private int maxWidthOfCell = 0;
    private int maxHeightOfCell = 0;
    private SparseIntArray maxHeightSparseIntArray = new SparseIntArray();
    private SparseIntArray maxWidthSparseIntArray = new SparseIntArray();

    //endregion Variables for drawing

    //region Constructor and setup methods

    public StickyHeaderTableView(Context context) {
        super(context, null, 0);
        setup(context, null, 0);
    }

    public StickyHeaderTableView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        setup(context, attrs, 0);
    }

    public StickyHeaderTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        final DisplayMatrixHelper displayMatrixHelper = new DisplayMatrixHelper();

        final int defaultTextSize = (int) displayMatrixHelper.dpToPixels(getContext(), 14);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.StickyHeaderTableView, defStyleAttr, defStyleAttr);

        if (a != null) {
            try {
                textLabelColor = a.getColor(
                        R.styleable.StickyHeaderTableView_shtv_textLabelColor, Color.BLACK);
                textHeaderColor = a.getColor(
                        R.styleable.StickyHeaderTableView_shtv_textHeaderColor, Color.BLACK);
                dividerColor = a.getColor(
                        R.styleable.StickyHeaderTableView_shtv_dividerColor, Color.BLACK);

                textLabelSize = a.getDimensionPixelSize(
                        R.styleable.StickyHeaderTableView_shtv_textLabelSize, defaultTextSize);
                textHeaderSize = a.getDimensionPixelSize(
                        R.styleable.StickyHeaderTableView_shtv_textHeaderSize, defaultTextSize);
                dividerThickness = a.getDimensionPixelSize(R.styleable.StickyHeaderTableView_shtv_dividerThickness, 0);
                cellPadding = a.getDimensionPixelSize(R.styleable.StickyHeaderTableView_shtv_cellPadding, 0);

                isDisplayLeftHeadersVertically = a.getBoolean(R.styleable.StickyHeaderTableView_shtv_isDisplayLeftHeadersVertically, false);
                isWrapHeightOfEachRow = a.getBoolean(R.styleable.StickyHeaderTableView_shtv_isWrapHeightOfEachRow, false);
                isWrapWidthOfEachColumn = a.getBoolean(R.styleable.StickyHeaderTableView_shtv_isWrapWidthOfEachColumn, false);

                headerCellFillColor = a.getColor(
                        R.styleable.StickyHeaderTableView_shtv_headerCellFillColor, Color.TRANSPARENT);

                contentCellFillColor = a.getColor(
                        R.styleable.StickyHeaderTableView_shtv_contentCellFillColor, Color.TRANSPARENT);

            } catch (Exception e) {
                setupDefaultVariableValue(defaultTextSize);
            } finally {
                a.recycle();
            }
        } else {
            setupDefaultVariableValue(defaultTextSize);
        }

        setupPaint();
    }

    private void setupDefaultVariableValue(int defaultTextSize) {
        textLabelColor = Color.BLACK;
        textHeaderColor = Color.BLACK;
        dividerColor = Color.BLACK;
        textLabelSize = defaultTextSize;
        textHeaderSize = defaultTextSize;
        dividerThickness = 0;
        cellPadding = 0;
        headerCellFillColor = Color.TRANSPARENT;
        contentCellFillColor = Color.TRANSPARENT;
    }

    private void setupPaint() {
        paintStrokeRect.setStyle(Paint.Style.STROKE);
        paintStrokeRect.setColor(dividerColor);
        paintStrokeRect.setStrokeWidth(dividerThickness);

        paintHeaderCellFillRect.setStyle(Paint.Style.FILL);
        paintHeaderCellFillRect.setColor(headerCellFillColor);

        paintContentCellFillRect.setStyle(Paint.Style.FILL);
        paintContentCellFillRect.setColor(contentCellFillColor);

        paintLabelText.setStyle(Paint.Style.FILL);
        paintLabelText.setColor(textLabelColor);
        paintLabelText.setTextSize(textLabelSize);
        paintLabelText.setTextAlign(Paint.Align.LEFT);

        paintHeaderText.setStyle(Paint.Style.FILL);
        paintHeaderText.setColor(textHeaderColor);
        paintHeaderText.setTextSize(textHeaderSize);
        paintHeaderText.setTextAlign(Paint.Align.LEFT);
    }

    //endregion Constructor and setup methods

    //region Measure/Update the view

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = 0;
        int desiredHeight = 0;

        if (data != null) {
            updateMaxWidthHeightOfCell();
            if (isWrapHeightOfEachRow) {

                for (int i = 0; i < maxHeightSparseIntArray.size(); i++) {
                    desiredHeight = desiredHeight + maxHeightSparseIntArray.get(i, 0);
                }
                desiredHeight = desiredHeight + (dividerThickness / 2);
            } else {
                desiredHeight = maxHeightOfCell * data.size() + (dividerThickness / 2);
            }

            if (isWrapWidthOfEachColumn) {

                for (int i = 0; i < maxWidthSparseIntArray.size(); i++) {
                    desiredWidth = desiredWidth + maxWidthSparseIntArray.get(i, 0);
                }
                desiredWidth = desiredWidth + (dividerThickness / 2);

            } else {
                desiredWidth = maxWidthOfCell * data.get(0).size() + (dividerThickness / 2);
            }
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS both methods
        setDesiredDimension(desiredWidth, desiredHeight);
        setMeasuredDimension(width, height);
    }

    /**
     * Calculate and update max width height of cell<br/>
     * Required for onMeasure() method
     */
    private void updateMaxWidthHeightOfCell() {

        maxWidthOfCell = 0;
        maxHeightOfCell = 0;
        maxHeightSparseIntArray = new SparseIntArray();
        maxWidthSparseIntArray = new SparseIntArray();

        final int doubleCellPadding = cellPadding + cellPadding;

        for (int i = 0; i < data.size(); i++) {

            for (int j = 0; j < data.get(0).size(); j++) {

                if (i == 0 && j == 0) {

                    paintHeaderText.getTextBounds(data.get(i).get(j), 0, data.get(i).get(j).length(), textRectBounds);
                    if (maxWidthOfCell < textRectBounds.width()) {
                        maxWidthOfCell = textRectBounds.width();
                    }
                    if (maxHeightOfCell < textRectBounds.height()) {
                        maxHeightOfCell = textRectBounds.height();
                    }

                    if (maxWidthSparseIntArray.get(j, 0) < textRectBounds.width()) {
                        maxWidthSparseIntArray.put(j, textRectBounds.width());
                    }
                    if (maxHeightSparseIntArray.get(i, 0) < textRectBounds.height()) {
                        maxHeightSparseIntArray.put(i, textRectBounds.height());
                    }
                } else if (i == 0) {
                    // Top headers cells
                    paintHeaderText.getTextBounds(data.get(i).get(j), 0, data.get(i).get(j).length(), textRectBounds);
                    if (maxWidthOfCell < textRectBounds.width()) {
                        maxWidthOfCell = textRectBounds.width();
                    }
                    if (maxHeightOfCell < textRectBounds.height()) {
                        maxHeightOfCell = textRectBounds.height();
                    }

                    if (maxWidthSparseIntArray.get(j, 0) < textRectBounds.width()) {
                        maxWidthSparseIntArray.put(j, textRectBounds.width());
                    }
                    if (maxHeightSparseIntArray.get(i, 0) < textRectBounds.height()) {
                        maxHeightSparseIntArray.put(i, textRectBounds.height());
                    }

                } else if (j == 0) {
                    // Left headers cells
                    paintHeaderText.getTextBounds(data.get(i).get(j), 0, data.get(i).get(j).length(), textRectBounds);

                    if (isDisplayLeftHeadersVertically) {

                        if (maxWidthOfCell < textRectBounds.height()) {
                            maxWidthOfCell = textRectBounds.height();
                        }
                        if (maxHeightOfCell < textRectBounds.width()) {
                            maxHeightOfCell = textRectBounds.width();
                        }

                        if (maxWidthSparseIntArray.get(j, 0) < textRectBounds.height()) {
                            maxWidthSparseIntArray.put(j, textRectBounds.height());
                        }
                        if (maxHeightSparseIntArray.get(i, 0) < textRectBounds.width()) {
                            maxHeightSparseIntArray.put(i, textRectBounds.width());
                        }

                    } else {

                        if (maxWidthOfCell < textRectBounds.width()) {
                            maxWidthOfCell = textRectBounds.width();
                        }
                        if (maxHeightOfCell < textRectBounds.height()) {
                            maxHeightOfCell = textRectBounds.height();
                        }

                        if (maxWidthSparseIntArray.get(j, 0) < textRectBounds.width()) {
                            maxWidthSparseIntArray.put(j, textRectBounds.width());
                        }
                        if (maxHeightSparseIntArray.get(i, 0) < textRectBounds.height()) {
                            maxHeightSparseIntArray.put(i, textRectBounds.height());
                        }
                    }
                } else {
                    // Other content cells
                    paintLabelText.getTextBounds(data.get(i).get(j), 0, data.get(i).get(j).length(), textRectBounds);
                    if (maxWidthOfCell < textRectBounds.width()) {
                        maxWidthOfCell = textRectBounds.width();
                    }
                    if (maxHeightOfCell < textRectBounds.height()) {
                        maxHeightOfCell = textRectBounds.height();
                    }

                    if (maxWidthSparseIntArray.get(j, 0) < textRectBounds.width()) {
                        maxWidthSparseIntArray.put(j, textRectBounds.width());
                    }
                    if (maxHeightSparseIntArray.get(i, 0) < textRectBounds.height()) {
                        maxHeightSparseIntArray.put(i, textRectBounds.height());
                    }
                }
            }
        }
        maxWidthOfCell = maxWidthOfCell + doubleCellPadding;
        maxHeightOfCell = maxHeightOfCell + doubleCellPadding;

        for (int i = 0; i < maxHeightSparseIntArray.size(); i++) {
            maxHeightSparseIntArray.put(i, maxHeightSparseIntArray.get(i, 0) + doubleCellPadding);
        }

        for (int i = 0; i < maxWidthSparseIntArray.size(); i++) {
            maxWidthSparseIntArray.put(i, maxWidthSparseIntArray.get(i, 0) + doubleCellPadding);
        }
    }

    private void updateLayoutChanges() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!isInLayout()) {
                requestLayout();
            } else {
                invalidate();
            }
        } else {
            requestLayout();
        }
    }

    //endregion Measure/Update the view

    //region Canvas Drawing

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null) {
            return;
        }

        //region Variables
        int cellLeftX;
        int cellTopY = scrolledRect.top;
        int cellRightX;
        int cellBottomY = scrolledRect.top + getHeightOfRow(0);
        int halfDividerThickness = dividerThickness / 2;

        float drawTextX;
        float drawTextY;
        String textToDraw;
        //endregion Variables

        //region Update each cell rectangle as per scrolledRect

        // This is top-left most cell (0,0)
        updateCellRectangle(0, 0, halfDividerThickness, halfDividerThickness, getWidthOfColumn(0), getHeightOfRow(0));

        for (int i = 0; i < data.size(); i++) {
            cellRightX = scrolledRect.left;
            int heightOfRowI = getHeightOfRow(i);
            if (i == 0) {
                cellTopY = halfDividerThickness;
                for (int j = 0; j < data.get(i).size(); j++) {
                    cellLeftX = cellRightX - halfDividerThickness;
                    cellRightX += getWidthOfColumn(j);
                    if (j != 0) {
                        // This are top header cells (0,*)
                        updateCellRectangle(i, j, cellLeftX, cellTopY, cellRightX, heightOfRowI);
                    }
                }
                cellBottomY = scrolledRect.top + getHeightOfRow(i);
            } else {
                // These are content cells
                for (int j = 0; j < data.get(0).size(); j++) {
                    cellLeftX = cellRightX - halfDividerThickness;
                    cellRightX += getWidthOfColumn(j);
                    if (j != 0) {
                        updateCellRectangle(i, j, cellLeftX, cellTopY, cellRightX, cellBottomY);
                    }
                }

                // This are left header cells (*,0)
                cellRightX = 0;
                cellLeftX = cellRightX + halfDividerThickness;
                cellRightX += getWidthOfColumn(0);
                updateCellRectangle(i, 0, cellLeftX, cellTopY, cellRightX, cellBottomY);
            }
            cellTopY = cellBottomY - halfDividerThickness;
            cellBottomY = cellBottomY + getHeightOfRow(i + 1);
        }

        //endregion Update each cell rectangle as per scrolledRect

        //region Draw contents & left headers

        boolean isLeftVisible;
        boolean isTopVisible;
        boolean isRightVisible;
        boolean isBottomVisible;

        for (int i = 1; i < data.size(); i++) {
            isTopVisible = cellsRectangles[i][0].top >= cellsRectangles[0][0].bottom
                    && cellsRectangles[i][0].top <= visibleContentRect.bottom;
            isBottomVisible = cellsRectangles[i][0].bottom >= cellsRectangles[0][0].bottom
                    && cellsRectangles[i][0].bottom <= visibleContentRect.bottom;

            if (isTopVisible || isBottomVisible) {

                // Draw contents
                for (int j = 1; j < data.get(i).size(); j++) {
                    isLeftVisible = cellsRectangles[i][j].left >= cellsRectangles[i][0].right
                            && cellsRectangles[i][j].left <= visibleContentRect.right;
                    isRightVisible = cellsRectangles[i][j].right >= cellsRectangles[i][0].right
                            && cellsRectangles[i][j].right <= visibleContentRect.right;

                    if (isLeftVisible || isRightVisible) {
                        canvas.drawRect(cellsRectangles[i][j].left, cellsRectangles[i][j].top, cellsRectangles[i][j].right, cellsRectangles[i][j].bottom, paintContentCellFillRect);
                        if (dividerThickness != 0) {
                            canvas.drawRect(cellsRectangles[i][j].left, cellsRectangles[i][j].top, cellsRectangles[i][j].right, cellsRectangles[i][j].bottom, paintStrokeRect);
                        }

                        textToDraw = data.get(i).get(j);
                        paintLabelText.getTextBounds(textToDraw, 0, textToDraw.length(), textRectBounds);

                        drawTextX = cellsRectangles[i][j].right - (getWidthOfColumn(j) / 2f) - (textRectBounds.width() / 2f);
                        drawTextY = cellsRectangles[i][j].bottom - (getHeightOfRow(i) / 2f) + (textRectBounds.height() / 2f);

                        canvas.drawText(textToDraw, 0, textToDraw.length(), drawTextX, drawTextY, paintLabelText);
                    }
                }

                // Draw left header (*,0)
                canvas.drawRect(cellsRectangles[i][0].left, cellsRectangles[i][0].top, cellsRectangles[i][0].right, cellsRectangles[i][0].bottom, paintHeaderCellFillRect);
                if (dividerThickness != 0) {
                    canvas.drawRect(cellsRectangles[i][0].left, cellsRectangles[i][0].top, cellsRectangles[i][0].right, cellsRectangles[i][0].bottom, paintStrokeRect);
                }

                textToDraw = data.get(i).get(0);
                paintHeaderText.getTextBounds(textToDraw, 0, textToDraw.length(), textRectBounds);

                if (isDisplayLeftHeadersVertically) {
                    drawTextX = cellsRectangles[i][0].right - (getWidthOfColumn(0) / 2f) + (textRectBounds.height() / 2f);
                    drawTextY = cellsRectangles[i][0].bottom - (getHeightOfRow(i) / 2f) + (textRectBounds.width() / 2f);
                    canvas.save();
                    canvas.rotate(-90, drawTextX, drawTextY);
                    canvas.drawText(textToDraw, 0, textToDraw.length(), drawTextX, drawTextY, paintHeaderText);
                    canvas.restore();
                } else {
                    drawTextX = cellsRectangles[i][0].right - (getWidthOfColumn(0) / 2f) - (textRectBounds.width() / 2f);
                    drawTextY = cellsRectangles[i][0].bottom - (getHeightOfRow(i) / 2f) + (textRectBounds.height() / 2f);
                    canvas.drawText(textToDraw, 0, textToDraw.length(), drawTextX, drawTextY, paintHeaderText);
                }
            }
        }

        //endregion Draw contents & left headers

        //region Draw top headers (0,*)

        for (int j = 1; j < data.get(0).size(); j++) {
            isLeftVisible = cellsRectangles[0][j].left >= cellsRectangles[0][0].right
                    && cellsRectangles[0][j].left <= visibleContentRect.right;
            isRightVisible = cellsRectangles[0][j].right >= cellsRectangles[0][0].right
                    && cellsRectangles[0][j].right <= visibleContentRect.right;

            if (isLeftVisible || isRightVisible) {
                canvas.drawRect(cellsRectangles[0][j].left, cellsRectangles[0][j].top, cellsRectangles[0][j].right, cellsRectangles[0][j].bottom, paintHeaderCellFillRect);
                if (dividerThickness != 0) {
                    canvas.drawRect(cellsRectangles[0][j].left, cellsRectangles[0][j].top, cellsRectangles[0][j].right, cellsRectangles[0][j].bottom, paintStrokeRect);
                }
                textToDraw = data.get(0).get(j);
                paintHeaderText.getTextBounds(textToDraw, 0, textToDraw.length(), textRectBounds);

                drawTextX = cellsRectangles[0][j].right - (getWidthOfColumn(j) / 2f) - (textRectBounds.width() / 2f);
                drawTextY = cellsRectangles[0][j].bottom - (getHeightOfRow(0) / 2f) + (textRectBounds.height() / 2f);

                canvas.drawText(textToDraw, 0, textToDraw.length(), drawTextX, drawTextY, paintHeaderText);
            }
        }

        //endregion Draw top headers (0,*)

        //region Draw top-left most cell (0,0)

        canvas.drawRect(cellsRectangles[0][0].left, cellsRectangles[0][0].top, cellsRectangles[0][0].right, cellsRectangles[0][0].bottom, paintHeaderCellFillRect);

        if (dividerThickness != 0) {
            canvas.drawRect(cellsRectangles[0][0].left, cellsRectangles[0][0].top, cellsRectangles[0][0].right, cellsRectangles[0][0].bottom, paintStrokeRect);
        }

        textToDraw = data.get(0).get(0);
        paintHeaderText.getTextBounds(textToDraw, 0, textToDraw.length(), textRectBounds);

        drawTextX = getWidthOfColumn(0) - (getWidthOfColumn(0) / 2f) - (textRectBounds.width() / 2f);
        drawTextY = getHeightOfRow(0) - (getHeightOfRow(0) / 2f) + (textRectBounds.height() / 2f);
        canvas.drawText(textToDraw, 0, textToDraw.length(), drawTextX, drawTextY, paintHeaderText);

        //endregion Draw top-left most cell (0,0)

        //region Draw whole view border
        if (dividerThickness != 0) {
            canvas.drawRect(visibleContentRect.left, visibleContentRect.top, visibleContentRect.right - halfDividerThickness, visibleContentRect.bottom - halfDividerThickness, paintStrokeRect);
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
    private void updateCellRectangle(int i, int j, int cellLeftX, int cellTopY, int cellRightX, int cellBottomY) {
        if (cellsRectangles[i][j] == null) {
            cellsRectangles[i][j] = new Rect(cellLeftX, cellTopY, cellRightX, cellBottomY);
        } else {
            cellsRectangles[i][j].left = cellLeftX;
            cellsRectangles[i][j].top = cellTopY;
            cellsRectangles[i][j].right = cellRightX;
            cellsRectangles[i][j].bottom = cellBottomY;
        }
    }

    private int getWidthOfColumn(int key) {
        if (isWrapWidthOfEachColumn) {
            return maxWidthSparseIntArray.get(key, 0);
        } else {
            return maxWidthOfCell;
        }
    }

    private int getHeightOfRow(int key) {
        if (isWrapHeightOfEachRow) {
            return maxHeightSparseIntArray.get(key, 0);
        } else {
            return maxHeightOfCell;
        }
    }

    //endregion Canvas Drawing

    private OnTableCellClickListener onTableCellClickListener = null;

    private final OnSingleTapUpListener tapListener = motionEvent -> {
        if (onTableCellClickListener != null) {

            final float x = motionEvent.getX();
            final float y = motionEvent.getY();

            boolean isEndLoop = false;

            for (int i = 0; i < cellsRectangles.length; i++) {

                if (cellsRectangles[i][0].top <= y && cellsRectangles[i][0].bottom >= y) {

                    for (int j = 0; j < cellsRectangles[0].length; j++) {

                        if (cellsRectangles[i][j].left <= x && cellsRectangles[i][j].right >= x) {
                            isEndLoop = true;
                            onTableCellClickListener.onTableCellClicked(i, j);
                            break;
                        }
                    }
                }
                if (isEndLoop) {
                    break;
                }
            }
        }
    };

    //region Getter/Setter methods

    /**
     * @return data which is previously set by setData(data) method. otherwise null.
     */
    public List<List<String>> getData() {
        return data;
    }

    /**
     * Set you table content data
     *
     * @param data table content data
     */
    public void setData(List<List<String>> data) {
        this.data = data;
        cellsRectangles = new Rect[data.size()][data.get(0).size()];
        updateLayoutChanges();
    }

    /**
     * set the cell click event
     *
     * @param onTableCellClickListener tableCellClickListener
     */
    public void setOnTableCellClickListener(OnTableCellClickListener onTableCellClickListener) {
        setOnSingleTapUpListener(tapListener);
        this.onTableCellClickListener = onTableCellClickListener;
    }

    /**
     * @return text color of the content cells
     */
    public int getTextLabelColor() {
        return textLabelColor;
    }

    /**
     * Set text color  for content cells
     *
     * @param textLabelColor color
     */
    public void setTextLabelColor(int textLabelColor) {
        this.textLabelColor = textLabelColor;
        invalidate();
    }

    /**
     * @return text color of the header cells
     */
    public int getTextHeaderColor() {
        return textHeaderColor;
    }

    /**
     * Set text color  for header cells
     *
     * @param textHeaderColor color
     */
    public void setTextHeaderColor(int textHeaderColor) {
        this.textHeaderColor = textHeaderColor;
        invalidate();
    }

    /**
     * @return color of the cell divider or cell border
     */
    public int getDividerColor() {
        return dividerColor;
    }

    /**
     * Set divider or border color  for cell
     *
     * @param dividerColor color
     */
    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    /**
     * @return text size in pixels of content cells
     */
    public int getTextLabelSize() {
        return textLabelSize;
    }

    /**
     * Set text size in pixels for content cells<br/>
     * You can use {@link DisplayMatrixHelper#dpToPixels(Context, float)} method to convert dp to pixel
     *
     * @param textLabelSize text size in pixels
     */
    public void setTextLabelSize(int textLabelSize) {
        this.textLabelSize = textLabelSize;
        updateLayoutChanges();
    }

    /**
     * @return text header size in pixels of header cells
     */
    public int getTextHeaderSize() {
        return textHeaderSize;
    }

    /**
     * Set text header size in pixels for header cells<br/>
     * You can use {@link DisplayMatrixHelper#dpToPixels(Context, float)} method to convert dp to pixel
     *
     * @param textHeaderSize text header size in pixels
     */
    public void setTextHeaderSize(int textHeaderSize) {
        this.textHeaderSize = textHeaderSize;
        updateLayoutChanges();
    }

    /**
     * @return divider thickness in pixels
     */
    public int getDividerThickness() {
        return dividerThickness;
    }

    /**
     * Set divider thickness size in pixels for all cells<br/>
     * You can use {@link DisplayMatrixHelper#dpToPixels(Context, float)} method to convert dp to pixel
     *
     * @param dividerThickness divider thickness size in pixels
     */
    public void setDividerThickness(int dividerThickness) {
        this.dividerThickness = dividerThickness;
        invalidate();
    }

    /**
     * @return header cell's fill color
     */
    public int getHeaderCellFillColor() {
        return headerCellFillColor;
    }

    /**
     * Set header cell fill color
     *
     * @param headerCellFillColor color to fill in header cell
     */
    public void setHeaderCellFillColor(int headerCellFillColor) {
        this.headerCellFillColor = headerCellFillColor;
        invalidate();
    }

    /**
     * @return content cell's fill color
     */
    public int getContentCellFillColor() {
        return contentCellFillColor;
    }

    /**
     * Set content cell fill color
     *
     * @param contentCellFillColor color to fill in content cell
     */
    public void setContentCellFillColor(int contentCellFillColor) {
        this.contentCellFillColor = contentCellFillColor;
        invalidate();
    }

    /**
     * @return cell padding in pixels
     */
    public int getCellPadding() {
        return cellPadding;
    }

    /**
     * Set padding for all cell of table<br/>
     * You can use {@link DisplayMatrixHelper#dpToPixels(Context, float)} method to convert dp to pixel
     *
     * @param cellPadding cell padding in pixels
     */
    public void setCellPadding(int cellPadding) {
        this.cellPadding = cellPadding;
        updateLayoutChanges();
    }

    /**
     * @return true if left header cell text are displayed vertically enabled
     */
    public boolean isDisplayLeftHeadersVertically() {
        return isDisplayLeftHeadersVertically;
    }

    /**
     * Set left header text display vertically or horizontal
     *
     * @param displayLeftHeadersVertically true if you wants to set left header text display vertically
     */
    public void setDisplayLeftHeadersVertically(boolean displayLeftHeadersVertically) {
        isDisplayLeftHeadersVertically = displayLeftHeadersVertically;
        updateLayoutChanges();
    }

    /**
     * @return true if you settled true for wrap height of each row
     */
    public boolean isWrapHeightOfEachRow() {
        return isWrapHeightOfEachRow;
    }

    /**
     * Set whether height of each row should wrap or not
     *
     * @param wrapHeightOfEachRow pass true if you wants to set each row should wrap the height
     */
    public void setWrapHeightOfEachRow(boolean wrapHeightOfEachRow) {
        isWrapHeightOfEachRow = wrapHeightOfEachRow;
        updateLayoutChanges();
    }

    /**
     * @return true if you settled true for wrap width of each column
     */
    public boolean isWrapWidthOfEachColumn() {
        return isWrapWidthOfEachColumn;
    }

    /**
     * Set whether width of each column should wrap or not
     *
     * @param wrapWidthOfEachColumn pass true if you wants to set each column should wrap the width
     */
    public void setWrapWidthOfEachColumn(boolean wrapWidthOfEachColumn) {
        isWrapWidthOfEachColumn = wrapWidthOfEachColumn;
        updateLayoutChanges();
    }

    //endregion Getter/Setter methods
}
