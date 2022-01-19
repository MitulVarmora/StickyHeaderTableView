package sticky.header.tableview.stickyheadertableview;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DisplayMatrixHelper {

    public float dpToPixels(Context context, float dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
    }
}
