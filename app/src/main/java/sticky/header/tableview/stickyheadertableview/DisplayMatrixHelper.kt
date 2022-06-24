package sticky.header.tableview.stickyheadertableview

import android.content.Context
import android.util.TypedValue

class DisplayMatrixHelper {
    fun dpToPixels(context: Context, dpValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics)
    }
}