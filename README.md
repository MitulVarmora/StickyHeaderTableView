# StickyHeaderTableView
The StickyHeaderTableView is a custom view which is created by extending View class of android

## Purpose:
This view is created to display data in table with sticky 1st column and row,
content is bi-directionally scrollable. Android doesn't provide sticky header support
in TableView, to overcome this kind of data representation I have created this view.

## Uses:
1. copy-past the package "stickyheadertableview" in your package.

2. Copy the styleable StickyHeaderTableView from "res/value/attr.xml" file and past it to your res/value/attr.xml file

3. That's it. You can check MainActivity.java and activity_main.xml for demo. Or you can use below codes:

    In your xml layout file

        <view.package.name.StickyHeaderTableView
            xmlns:custom="http://schemas.android.com/apk/res-auto"
            android:id="@+id/stickyHeaderTableView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:cellPadding="10dp"
            app:contentCellFillColor="@color/contentCellFillColor"
            app:dividerColor="@color/lineDividerColor"
            app:dividerThickness="1dp"
            app:headerCellFillColor="@color/headerCellFillColor"
            app:is2DScrollEnabled="false"
            app:isDisplayLeftHeadersVertically="true"
            app:isWrapHeightOfEachRow="true"
            app:isWrapWidthOfEachColumn="true"
            app:textHeaderColor="@color/textHeaderColor"
            app:textHeaderSize="18dp"
            app:textLabelColor="@color/textLabelColor"
            app:textLabelSize="14dp" />

    In your activity/fragment

        val stickyHeaderTableView = findViewById<View>(R.id.stickyHeaderTableView) as StickyHeaderTableView
        stickyHeaderTableView.data = getDummyData()
        stickyHeaderTableView.setOnTableCellClickListener { rowPosition, columnPosition ->
            Toast.makeText(
                this@MainActivity,
                "Row: $rowPosition, Column: $columnPosition",
                Toast.LENGTH_SHORT
            ).show()
        }

        // For Nested Scrolling
        //stickyHeaderTableView.isNestedScrollingEnabled = true
        //stickyHeaderTableView.setNestedScrollAxis(ViewCompat.SCROLL_AXIS_VERTICAL)

    And here is getDummyData() method

        private fun getDummyData(): Array<Array<String?>> {
            val row = 31
            val column = 31

            val strings = Array(row) {
                arrayOfNulls<String>(column)
            }

            for (i in -1 until row - 1) {
                for (j in -1 until column - 1) {
                    if (i == -1 && j == -1) {
                        strings[0][0] = "0,0"
                    } else if (i == -1) {
                        strings[i + 1][j + 1] = "C " + (j + 1).toString()
                    } else if (j == -1) {
                        strings[i + 1][j + 1] = "R " + (i + 1).toString()
                    } else {
                        strings[i + 1][j + 1] = (i + 1).toString() + "," + (j + 1).toString()
                    }
                }
            }

            strings[3][0] = "R 3 Big"
            strings[0][2] = "C 2 Big"
            strings[7][5] = "7,5 larger"

            return strings
        }

    Below are all available setter methods (It has respective getter methods too)

        setData(String[][] data)
        setOnTableCellClickListener(OnTableCellClickListener onTableCellClickListener)
        setIs2DScrollingEnabled(boolean is2DScrollingEnabled)
        setTextLabelColor(int textLabelColor)
        setTextHeaderColor(int textHeaderColor)
        setDividerColor(int dividerColor)
        setTextLabelSize(int textLabelSize)
        setTextHeaderSize(int textHeaderSize)
        setDividerThickness(int dividerThickness)
        setHeaderCellFillColor(int headerCellFillColor)
        setContentCellFillColor(int contentCellFillColor)
        setCellPadding(int cellPadding)
        setDisplayLeftHeadersVertically(boolean displayLeftHeadersVertically)
        setWrapHeightOfEachRow(boolean wrapHeightOfEachRow)
        setWrapWidthOfEachColumn(boolean wrapWidthOfEachColumn)
        setNestedScrollingEnabled(boolean enabled)

     Useful methods for scrolling

        canScrollHorizontally()
        canScrollVertically()
        scrollHorizontal(float distanceX)
        scrollVertical(float distanceY)
        scroll2D(float distanceX, float distanceY)
        getVisibleContentRect()
        getScrolledRect()
        getActualContentRect()
        setNestedScrollAxis(int nestedScrollAxis)

    If you wants to use this view as nested scrolling child, you must use below 2 methods

        setNestedScrollingEnabled(boolean enabled)
        setNestedScrollAxis(int nestedScrollAxis) // ViewCompat.SCROLL_AXIS_HORIZONTAL or ViewCompat.SCROLL_AXIS_VERTICAL

    Precaution:<br/>
    1. If you are putting this view in the Vertical ScrollView than you can not set this view's height fixed.
    <br/>You should use height wrap_content then it will work fine.

    2. If you are putting this view in the Horizontal ScrollView than you can not set this view's width fixed.
    <br/>You should use width wrap_content then it will work fine.


## Changelogs:
DD/MM/YYYY : Who : What<br/>
11/08/2016 : Mitul : Created View by Mitul Varmora<br/>
19/01/2022 : Mitul : Rebase Custom View on top of latest new Android Studio Project and use jetpack<br/>
