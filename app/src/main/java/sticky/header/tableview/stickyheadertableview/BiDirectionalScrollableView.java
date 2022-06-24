package sticky.header.tableview.stickyheadertableview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import sticky.header.tableview.R;

public abstract class BiDirectionalScrollableView extends View implements NestedScrollingChild {

    //region Variables for boundary of the View

    /**
     * Visible rect size of view which is displayed on screen
     */
    protected final Rect visibleContentRect = new Rect(0, 0, 0, 0);
    /**
     * based on scrolling this rect value will update
     */
    protected final Rect scrolledRect = new Rect(0, 0, 0, 0);
    /**
     * Actual rect size of canvas drawn content (Which may be larger or smaller than mobile screen)
     */
    protected final Rect actualContentRect = new Rect(0, 0, 0, 0);

    //endregion Variables for boundary of the View

    //region Configurable variables via xml or setter methods

    public boolean is2DScrollingEnabled = true;

    //endregion Configurable variables via xml or setter methods

    //region Constructor and setup methods

    public BiDirectionalScrollableView(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public BiDirectionalScrollableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public BiDirectionalScrollableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray attrTypedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.StickyHeaderTableView, defStyleAttr, defStyleAttr);

        if (attrTypedArray != null) {
            try {
                is2DScrollingEnabled = attrTypedArray.getBoolean(
                        R.styleable.StickyHeaderTableView_shtv_is2DScrollEnabled,
                        true
                );
            } catch (Exception ignored) {
            } finally {
                attrTypedArray.recycle();
            }
        }
    }

    //endregion Constructor and setup methods

    //region View dimensions initialization/updates

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        visibleContentRect.set(0, 0, w, h);
    }

    /**
     * This methods must be called from onMeasured overridden method to tell actual content size
     *
     * @param desiredWidth  width of all content
     * @param desiredHeight height of all content
     */
    protected void setDesiredDimension(int desiredWidth, int desiredHeight) {
        scrolledRect.set(0, 0, desiredWidth, desiredHeight);
        actualContentRect.set(0, 0, desiredWidth, desiredHeight);
    }

    //endregion View dimensions initialization/updates

    //region Scrolling, Flinging and Click events

    private long startTime;
    private long endTime;
    private float totalAnimDx;
    private float totalAnimDy;
    private float lastAnimDx;
    private float lastAnimDy;
    private boolean isScrollingHorizontally = false;
    private boolean isScrollingVertically = false;
    /**
     * This is used to stop fling animation if user touch view during fling animation
     */
    private boolean isFlinging = false;
    private final DecelerateInterpolator flingAnimInterpolator = new DecelerateInterpolator();
    private final GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        public boolean onDown(MotionEvent e) {
            if (isNestedScrollingEnabled()) {
                startNestedScroll(NESTED_SCROLL_AXIS);
            }
            if (isFlinging) {
                isFlinging = false;
            }
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isNestedScrollingEnabled()) {
                dispatchNestedPreFling(velocityX, velocityY);
            }

            if (!canScrollHorizontally() && !canScrollVertically()) {
                return false;
            }

            final float distanceTimeFactor = 0.4f;
            totalAnimDx = (distanceTimeFactor * velocityX / 2);
            totalAnimDy = (distanceTimeFactor * velocityY / 2);
            lastAnimDx = 0;
            lastAnimDy = 0;
            startTime = System.currentTimeMillis();
            endTime = startTime + (long) (1000 * distanceTimeFactor);

            float deltaY = e2.getY() - e1.getY();
            float deltaX = e2.getX() - e1.getX();

            if (!is2DScrollingEnabled) {
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    isScrollingHorizontally = true;
                } else {
                    isScrollingVertically = true;
                }
            }
            isFlinging = true;

            if (onFlingAnimateStep()) {
                if (isNestedScrollingEnabled()) {
                    dispatchNestedFling(-velocityX, -velocityY, true);
                }
                return true;
            } else {
                if (isNestedScrollingEnabled()) {
                    dispatchNestedFling(-velocityX, -velocityY, false);
                }
                return false;
            }

        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (isNestedScrollingEnabled()) {
                dispatchNestedPreScroll((int) distanceX, (int) distanceY, null, null);
            }

            boolean isScrolled;

            if (is2DScrollingEnabled) {
                isScrolled = scroll2D(distanceX, distanceY);
            } else {

                if (isScrollingHorizontally) {
                    isScrolled = scrollHorizontal(distanceX);
                } else if (isScrollingVertically) {
                    isScrolled = scrollVertical(distanceY);
                } else {

                    float deltaY = e2.getY() - e1.getY();
                    float deltaX = e2.getX() - e1.getX();

                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        // if deltaX > 0 : the user made a sliding right gesture
                        // else : the user made a sliding left gesture
                        isScrollingHorizontally = true;
                        isScrolled = scrollHorizontal(distanceX);
                    } else {
                        // if deltaY > 0 : the user made a sliding down gesture
                        // else : the user made a sliding up gesture
                        isScrollingVertically = true;
                        isScrolled = scrollVertical(distanceY);
                    }
                }
            }

            // Fix scrolling (if any parent view is scrollable in layout hierarchy,
            // than this will disallow intercepting touch event)
            if (getParent() != null && isScrolled) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }

            if (isScrolled) {
                if (isNestedScrollingEnabled()) {
                    dispatchNestedScroll((int) distanceX, (int) distanceY, 0, 0, null);
                }
            } else {
                if (isNestedScrollingEnabled()) {
                    dispatchNestedScroll(0, 0, (int) distanceX, (int) distanceY, null);
                }
            }

            return isScrolled;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            if (tapListener != null) {
                tapListener.onSingleTapUp(e);
            }
            return super.onSingleTapUp(e);
        }

        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }
    };
    private final GestureDetector gestureDetector = new GestureDetector(getContext(), simpleOnGestureListener);
    OnSingleTapUpListener tapListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isScrollingHorizontally = false;
                isScrollingVertically = false;
                break;
        }

        return gestureDetector.onTouchEvent(event);
        //return true;
    }

    /**
     * This will start fling animation
     *
     * @return true if fling animation consumed
     */
    private boolean onFlingAnimateStep() {

        boolean isScrolled = false;

        long curTime = System.currentTimeMillis();
        float percentTime = (float) (curTime - startTime) / (float) (endTime - startTime);
        float percentDistance = flingAnimInterpolator.getInterpolation(percentTime);
        float curDx = percentDistance * totalAnimDx;
        float curDy = percentDistance * totalAnimDy;

        float distanceX = curDx - lastAnimDx;
        float distanceY = curDy - lastAnimDy;
        lastAnimDx = curDx;
        lastAnimDy = curDy;

        if (is2DScrollingEnabled) {
            isScrolled = scroll2D(-distanceX, -distanceY);
        } else if (isScrollingHorizontally) {
            isScrolled = scrollHorizontal(-distanceX);
        } else if (isScrollingVertically) {
            isScrolled = scrollVertical(-distanceY);
        }

        // This will stop fling animation if user has touch intercepted
        if (!isFlinging) {
            return false;
        }

        if (percentTime < 1.0f) {
            // fling animation running
            post(this::onFlingAnimateStep);
        } else {
            // fling animation ended
            isFlinging = false;
            isScrollingVertically = false;
            isScrollingHorizontally = false;
        }
        return isScrolled;
    }

    //endregion Scrolling, Flinging and Click events

    //region Scrolling methods

    /**
     * Check if content width is bigger than  view width
     *
     * @return true if content width is bigger than view width
     */
    public boolean canScrollHorizontally() {
        return actualContentRect.right > visibleContentRect.right;
    }

    /**
     * Check if content height is bigger than  view height
     *
     * @return true if content height is bigger than view height
     */
    public boolean canScrollVertically() {
        return actualContentRect.bottom > visibleContentRect.bottom;
    }

    /**
     * Scroll horizontally
     *
     * @param distanceX distance to scroll
     * @return true if horizontally scrolled, false otherwise
     */
    public boolean scrollHorizontal(float distanceX) {

        if (!canScrollHorizontally() || distanceX == 0) {
            return false;
        }

        int newScrolledLeft = scrolledRect.left - (int) distanceX;
        int newScrolledRight = scrolledRect.right - (int) distanceX;

        if (newScrolledLeft > 0) {
            newScrolledLeft = 0;
            newScrolledRight = actualContentRect.right;
        } else if (newScrolledLeft < -(actualContentRect.right - visibleContentRect.right)) {
            newScrolledLeft = -(actualContentRect.right - visibleContentRect.right);
            newScrolledRight = visibleContentRect.right;
        }

        if (scrolledRect.left == newScrolledLeft) {
            return false;
        }
        scrolledRect.set(newScrolledLeft, scrolledRect.top, newScrolledRight, scrolledRect.bottom);
        invalidate();
        return true;
    }

    /**
     * Scroll vertically
     *
     * @param distanceY distance to scroll
     * @return true if vertically scrolled, false otherwise
     */
    public boolean scrollVertical(float distanceY) {

        if (!canScrollVertically() || distanceY == 0) {
            return false;
        }

        int newScrolledTop = scrolledRect.top - (int) distanceY;
        int newScrolledBottom = scrolledRect.bottom - (int) distanceY;

        if (newScrolledTop > 0) {
            newScrolledTop = 0;
            newScrolledBottom = actualContentRect.bottom;
        } else if (newScrolledTop < -(actualContentRect.bottom - visibleContentRect.bottom)) {
            newScrolledTop = -(actualContentRect.bottom - visibleContentRect.bottom);
            newScrolledBottom = visibleContentRect.bottom;
        }

        if (scrolledRect.top == newScrolledTop) {
            return false;
        }
        scrolledRect.set(scrolledRect.left, newScrolledTop, scrolledRect.right, newScrolledBottom);
        invalidate();
        return true;
    }

    /**
     * Scroll vertically & horizontal both side
     *
     * @param distanceX distance to scroll
     * @param distanceY distance to scroll
     * @return true if scrolled, false otherwise
     */
    public boolean scroll2D(float distanceX, float distanceY) {

        boolean isScrollHappened = false;
        int newScrolledLeft;
        int newScrolledTop;
        int newScrolledRight;
        int newScrolledBottom;

        if (canScrollHorizontally()) {
            newScrolledLeft = scrolledRect.left - (int) distanceX;
            newScrolledRight = scrolledRect.right - (int) distanceX;

            if (newScrolledLeft > 0) {
                newScrolledLeft = 0;
            }
            if (newScrolledLeft < -(actualContentRect.right - visibleContentRect.right)) {
                newScrolledLeft = -(actualContentRect.right - visibleContentRect.right);
            }
            isScrollHappened = true;
        } else {
            newScrolledLeft = scrolledRect.left;
            newScrolledRight = scrolledRect.right;
        }

        if (canScrollVertically()) {
            newScrolledTop = scrolledRect.top - (int) distanceY;
            newScrolledBottom = scrolledRect.bottom - (int) distanceY;

            if (newScrolledTop > 0) {
                newScrolledTop = 0;
            }
            if (newScrolledTop < -(actualContentRect.bottom - visibleContentRect.bottom)) {
                newScrolledTop = -(actualContentRect.bottom - visibleContentRect.bottom);
            }
            isScrollHappened = true;
        } else {
            newScrolledTop = scrolledRect.top;
            newScrolledBottom = scrolledRect.bottom;
        }

        if (!isScrollHappened) {
            return false;
        }

        scrolledRect.set(newScrolledLeft, newScrolledTop, newScrolledRight, newScrolledBottom);
        invalidate();
        return true;
    }

    /**
     * @return true if content are scrollable from top to bottom side
     */
    public boolean canScrollTop() {
        return scrolledRect.top < visibleContentRect.top;
    }

    /**
     * @return true if content are scrollable from bottom to top side
     */
    public boolean canScrollBottom() {
        return scrolledRect.bottom > visibleContentRect.bottom;
    }

    /**
     * @return true if content are scrollable from left to right side
     */
    public boolean canScrollRight() {
        return scrolledRect.right > visibleContentRect.right;
    }

    /**
     * @return true if content are scrollable from right to left side
     */
    public boolean canScrollLeft() {
        return scrolledRect.left < visibleContentRect.left;
    }

    //endregion Scrolling methods

    //region NestedScrollingChild implementation

    private final NestedScrollingChildHelper nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
    private int NESTED_SCROLL_AXIS = ViewCompat.SCROLL_AXIS_NONE;

    /**
     * default Nested scroll axis is ViewCompat.SCROLL_AXIS_NONE <br/>
     * Nested scroll axis must be one of the <br/>ViewCompat.SCROLL_AXIS_NONE <br/>or ViewCompat.SCROLL_AXIS_HORIZONTAL <br/>or ViewCompat.SCROLL_AXIS_VERTICAL
     *
     * @param nestedScrollAxis value of nested scroll direction
     */
    public void setNestedScrollAxis(int nestedScrollAxis) {
        switch (nestedScrollAxis) {

            case ViewCompat.SCROLL_AXIS_HORIZONTAL:
                NESTED_SCROLL_AXIS = ViewCompat.SCROLL_AXIS_HORIZONTAL;
                break;
            case ViewCompat.SCROLL_AXIS_VERTICAL:
                NESTED_SCROLL_AXIS = ViewCompat.SCROLL_AXIS_VERTICAL;
                break;
            default:
                NESTED_SCROLL_AXIS = ViewCompat.SCROLL_AXIS_NONE;
                break;
        }
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return nestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return nestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        nestedScrollingChildHelper.onDetachedFromWindow();
    }

    //endregion NestedScrollingChild implementation

    //region Getter/Setter methods

    protected void setOnSingleTapUpListener(OnSingleTapUpListener tapListener) {
        this.tapListener = tapListener;
    }

    /**
     * enable or disable 2 directional scroll
     *
     * @param is2DScrollingEnabled true if you wants to enable 2 directional scroll
     */
    public void setIs2DScrollingEnabled(boolean is2DScrollingEnabled) {
        this.is2DScrollingEnabled = is2DScrollingEnabled;
    }

    /**
     * Check whether is 2 directional scroll is enabled or not
     *
     * @return true if 2 directional scroll is enabled
     */
    public boolean is2DScrollingEnabled() {
        return is2DScrollingEnabled;
    }

    /**
     * @return the Rect object which is visible area on screen
     */
    public Rect getVisibleContentRect() {
        return visibleContentRect;
    }

    /**
     * @return the Rect object which is last scrolled area from actual content rectangle
     */
    public Rect getScrolledRect() {
        return scrolledRect;
    }

    /**
     * @return the Rect object which is actual content area
     */
    public Rect getActualContentRect() {
        return actualContentRect;
    }

    //endregion Getter/Setter methods

}

interface OnSingleTapUpListener {
    void onSingleTapUp(MotionEvent e);
}
