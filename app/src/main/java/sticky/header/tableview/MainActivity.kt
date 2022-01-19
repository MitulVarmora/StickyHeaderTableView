package sticky.header.tableview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import sticky.header.tableview.stickyheadertableview.StickyHeaderTableView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()
    }

    private fun initComponents() {
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
    }

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
}