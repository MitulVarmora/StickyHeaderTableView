# StickyHeaderTableView
The StickyHeaderTableView is a custom view which is created by extending View class of android

## Purpose:
This view is created to display data in table with sticky 1st column and row,
content is bi-directionally scrollable. Android doesn't provide sticky header support
in TableView, to overcome this kind of data representation I have created this view.

## Uses:
1. copy-past the package "stickyheadertableview" in your package.

2. Copy the styleable StickyHeaderTableView from "res/value/attr.xml" file and past it to your res/value/attr.xml file

3. That's it. You can check MainActivity.java and activity_main.xml for demo.<br/><br/>Or you can use below codes

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

    You can use java code like this

        StickyHeaderTableView stickyHeaderTableView = (StickyHeaderTableView) findViewById(R.id.stickyHeaderTableView);
        stickyHeaderTableView.setData(strings);
        stickyHeaderTableView.setOnTableCellClickListener(new OnTableCellClickListener() {
           @Override
           public void onTableCellClicked(int rowPosition, int columnPosition) {
               Toast.makeText(context, "Row: " + rowPosition + ", Column: " + columnPosition, Toast.LENGTH_SHORT).show();
           }
        });

    stickyHeaderTableView.setData(strings) is String[][] array you have to set.<br/><br/>
    For demo you can prepare it like below

        int row = 15;
        int column = 20;
        String[][] strings = new String[row][column];
        for (int i = -1; i < row - 1; i++) {
            for (int j = -1; j < column - 1; j++) {
                if (i == -1 && j == -1) {
                    // Top left most cell
                    strings[0][0] = "0,0";
                } else if (i == -1) {
                    // Top most cells (top headers)
                    strings[i + 1][j + 1] = "C " + String.valueOf(j+1);
                } else if (j == -1) {
                    // Left most cells (left headers)
                    strings[i + 1][j + 1] = "R " + String.valueOf(i+1);
                } else {
                    // Other value content cells
                    strings[i + 1][j + 1] = String.valueOf(i+1) + "," + String.valueOf(j+1);
                }
            }
        }
        // Optional header cell modifications
        strings[3][0] = "R 3 Big"
        strings[0][2] = "C 2 Big"
        strings[7][5] = "7,5 larger"

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

    Precaution:<br/><br/>

    1. If you are putting this view in the Vertical ScrollView than you can not set this view's height fixed.
    <br/><br/>You should use height wrap_content than it will work fine.

    2. If you are putting this view in the Horizontal ScrollView than you can not set this view's width fixed.
    <br/><br/>You should use width wrap_content than it will work fine.


## Changelogs:
DD/MM/YYYY :	Who		: What
11/08/2016 :	Mitul   : Created View by Mitul Varmora
19/01/2022 :    Mitul   : Rebase Custom View on top of latest new Android Studio Project and use jetpack
