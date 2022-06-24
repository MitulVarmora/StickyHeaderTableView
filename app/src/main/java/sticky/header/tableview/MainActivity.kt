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

    private fun getDummyData(): List<List<String>> {
        val row = 31
        val column = 31

        val strings = ArrayList<ArrayList<String>>()
        var innerStrings: ArrayList<String>

        for (i in -1 until row - 1) {
            innerStrings = ArrayList()
            for (j in -1 until column - 1) {
                if (i == -1 && j == -1) {
                    innerStrings.add("0,0")
                } else if (i == -1) {
                    innerStrings.add("C " + (j + 1).toString())
                } else if (j == -1) {
                    innerStrings.add("R " + (i + 1).toString())
                } else {
                    innerStrings.add((i + 1).toString() + "," + (j + 1).toString())
                }
            }
            strings.add(innerStrings)
        }

        strings[3][0] = "R 3 Big"
        strings[0][2] = "C 2 Big"
        strings[7][5] = "7,5 larger"

        return strings
    }
}