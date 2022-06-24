[![GitHub stars](https://img.shields.io/github/stars/MitulVarmora/StickyHeaderTableView)](https://github.com/MitulVarmora/StickyHeaderTableView/stargazers) [![GitHub forks](https://img.shields.io/github/forks/MitulVarmora/StickyHeaderTableView)](https://github.com/MitulVarmora/StickyHeaderTableView/network) [![GitHub issues](https://img.shields.io/github/issues/MitulVarmora/StickyHeaderTableView)](https://github.com/MitulVarmora/StickyHeaderTableView/issues) [![GitHub license](https://img.shields.io/github/license/MitulVarmora/StickyHeaderTableView)](https://github.com/MitulVarmora/StickyHeaderTableView/blob/main/LICENSE.md)

# StickyHeaderTableView
The StickyHeaderTableView is a custom view which is created by extending View class of android

## Purpose:
This view is created to display data in table with sticky 1st column and row,
content is bi-directionally scrollable. Android doesn't provide sticky header support
in TableView, to overcome this kind of data representation I have created this view.

## Screen Recording (Sample):
https://user-images.githubusercontent.com/23657151/151113930-61f8516b-1775-404e-a536-e74be7df9101.mp4

## Uses:
1. Copy-Past the package "stickyheadertableview" in your package.

2. Copy the styleable StickyHeaderTableView from "res/value/attr.xml" file and past it to your res/value/attr.xml file

3. Now you can use below codes or see [MainActivity.kt](/app/src/main/java/sticky/header/tableview/MainActivity.kt) and [activity_main.xml](/app/src/main/res/layout/activity_main.xml) for demo.

    In your xml layout file

        <view.package.name.StickyHeaderTableView
            xmlns:custom="http://schemas.android.com/apk/res-auto"
            android:id="@+id/stickyHeaderTableView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:shtv_cellPadding="10dp"
            app:shtv_contentCellFillColor="@color/contentCellFillColor"
            app:shtv_dividerColor="@color/lineDividerColor"
            app:shtv_dividerThickness="1dp"
            app:shtv_headerCellFillColor="@color/headerCellFillColor"
            app:shtv_is2DScrollEnabled="true"
            app:shtv_isDisplayLeftHeadersVertically="true"
            app:shtv_isWrapHeightOfEachRow="true"
            app:shtv_isWrapWidthOfEachColumn="true"
            app:shtv_textHeaderColor="@color/textHeaderColor"
            app:shtv_textHeaderSize="18dp"
            app:shtv_textLabelColor="@color/textLabelColor"
            app:shtv_textLabelSize="14dp" />

    In your activity/fragment

        val stickyHeaderTableView = findViewById<StickyHeaderTableView>(R.id.stickyHeaderTableView)
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
    1. If you are putting this view in the Vertical ScrollView then you can not set this view's height fixed.
    <br/>You should use height wrap_content then it will work fine.

    2. If you are putting this view in the Horizontal ScrollView then you can not set this view's width fixed.
    <br/>You should use width wrap_content then it will work fine.

## Changelogs:
DD/MM/YYYY : Who : What<br/>
11/08/2016 : Mitul : Created View by Mitul Varmora<br/>
19/01/2022 : Mitul : Rebase Custom View on top of latest new Android Studio Project and use jetpack<br/>
25/01/2022 : Mitul : Heavy performance improvements by just draw only cells which are visible to user<br/>

## Licence:
    MIT License

    Copyright (c) 2022 Mitul Varmora

    Permission is hereby granted, free of charge, to any person obtaining a copy 
    of this software and associated documentation files (the "Software"), to deal 
    in the Software without restriction, including without limitation the rights 
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
    copies of the Software, and to permit persons to whom the Software is 
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all 
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
    SOFTWARE.
