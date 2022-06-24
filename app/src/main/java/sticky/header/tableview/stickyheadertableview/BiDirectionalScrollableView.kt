package sticky.header.tableview.stickyheadertableview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import sticky.header.tableview.R
import kotlin.math.abs

abstract class BiDirectionalScrollableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), NestedScrollingChild {

    //region Variables for boundary of the View
    /**
     * Represents actual size of the view
     */
    protected val viewRect = Rect(0, 0, 0, 0)

    /**
     * Represents positive biggest possible size of content on canvas (Which may be larger or smaller than the viewRect)
     */
    private val contentRect = Rect(0, 0, 0, 0)

    /**
     * Initially same as contentRect, When scrolled it will change all points to + or -
     */
    protected val scrolledContentRect = Rect(0, 0, 0, 0)
    //endregion Variables for boundary of the View

    //region Configurable variables via xml or setter methods
    /**
     * enable or disable 2 directional scroll
     */
    var is2DScrollingEnabled = true
    //endregion Configurable variables via xml or setter methods

    //region Initial setup
    init {
        val attrTypedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.StickyHeaderTableView, defStyleAttr, defStyleAttr
        )
        try {
            is2DScrollingEnabled = attrTypedArray.getBoolean(
                R.styleable.StickyHeaderTableView_shtv_is2DScrollEnabled,
                true
            )
        } catch (ignored: Exception) {
        } finally {
            attrTypedArray.recycle()
        }
    }
    //endregion Initial setup

    //region View dimensions initialization/updates
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect.set(0, 0, w, h)
    }

    /**
     * This methods must be called from onMeasured overridden method to tell actual content size
     *
     * @param desiredWidth  width of all content
     * @param desiredHeight height of all content
     */
    protected fun setDesiredDimension(desiredWidth: Int, desiredHeight: Int) {
        scrolledContentRect.set(0, 0, desiredWidth, desiredHeight)
        contentRect.set(0, 0, desiredWidth, desiredHeight)
    }
    //endregion View dimensions initialization/updates

    //region Scrolling, Flinging and Click events
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var totalAnimDx = 0f
    private var totalAnimDy = 0f
    private var lastAnimDx = 0f
    private var lastAnimDy = 0f
    private var isScrollingHorizontally = false
    private var isScrollingVertically = false

    /**
     * This is used to stop fling animation if user touch view during fling animation
     */
    private var isFlinging = false
    private val flingAnimInterpolator = DecelerateInterpolator()
    private val simpleOnGestureListener: SimpleOnGestureListener =
        object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                if (isNestedScrollingEnabled) {
                    startNestedScroll(nestedScrollAxis)
                }
                if (isFlinging) {
                    isFlinging = false
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                if (isNestedScrollingEnabled) {
                    dispatchNestedPreFling(velocityX, velocityY)
                }
                if (!canScrollHorizontally() && !canScrollVertically()) {
                    return false
                }
                val distanceTimeFactor = 0.4f
                totalAnimDx = distanceTimeFactor * velocityX / 2
                totalAnimDy = distanceTimeFactor * velocityY / 2
                lastAnimDx = 0f
                lastAnimDy = 0f
                startTime = System.currentTimeMillis()
                endTime = startTime + (1000 * distanceTimeFactor).toLong()
                val deltaY = e2.y - e1.y
                val deltaX = e2.x - e1.x
                if (!is2DScrollingEnabled) {
                    if (abs(deltaX) > abs(deltaY)) {
                        isScrollingHorizontally = true
                    } else {
                        isScrollingVertically = true
                    }
                }
                isFlinging = true

                val isFlingConsumedByThisView = onFlingAnimateStep()
                if (isNestedScrollingEnabled) {
                    dispatchNestedFling(-velocityX, -velocityY, false)
                }
                return isFlingConsumedByThisView
            }

            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                if (isNestedScrollingEnabled) {
//                    if (nestedScrollAxis == ViewCompat.SCROLL_AXIS_HORIZONTAL) {
//                        dispatchNestedPreScroll(distanceX.toInt(), 0, null, null)
//                    } else if (nestedScrollAxis == ViewCompat.SCROLL_AXIS_VERTICAL) {
//                        dispatchNestedPreScroll(0, distanceY.toInt(), null, null)
//                    }
                    dispatchNestedPreScroll(distanceX.toInt(), distanceY.toInt(), null, null)

//                    if (nestedScrollAxis == ViewCompat.SCROLL_AXIS_HORIZONTAL) {
//                        dispatchNestedScroll(0, 0, distanceX.toInt(), 0, null)
//                    } else if (nestedScrollAxis == ViewCompat.SCROLL_AXIS_VERTICAL) {
//                        dispatchNestedScroll(0, 0, 0, distanceY.toInt(), null)
//                    }
                    dispatchNestedScroll(0, 0, distanceX.toInt(), distanceY.toInt(), null)
                }

                val isScrolled: Boolean
                if (is2DScrollingEnabled) {
                    isScrolled = scroll2D(distanceX, distanceY)
                } else {
                    if (isScrollingHorizontally) {
                        isScrolled = scrollHorizontal(distanceX)
                    } else if (isScrollingVertically) {
                        isScrolled = scrollVertical(distanceY)
                    } else {
                        val deltaY = e2.y - e1.y
                        val deltaX = e2.x - e1.x
                        if (abs(deltaX) > abs(deltaY)) {
                            // if deltaX > 0 : the user made a sliding right gesture
                            // else : the user made a sliding left gesture
                            isScrollingHorizontally = true
                            isScrolled = scrollHorizontal(distanceX)
                        } else {
                            // if deltaY > 0 : the user made a sliding down gesture
                            // else : the user made a sliding up gesture
                            isScrollingVertically = true
                            isScrolled = scrollVertical(distanceY)
                        }
                    }
                }

                // Fix scrolling (if any parent view is scrollable in layout hierarchy,
                // than this will disallow intercepting touch event)
                if (parent != null && isScrolled) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                return isScrolled
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onSingleTapUpEvent(e)
                return super.onSingleTapUp(e)
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return super.onDoubleTapEvent(e)
            }
        }
    private val gestureDetector = GestureDetector(getContext(), simpleOnGestureListener)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isScrollingHorizontally = false
                isScrollingVertically = false
            }
        }
        return gestureDetector.onTouchEvent(event)
        //return true;
    }

    /**
     * This will start fling animation
     *
     * @return true if fling animation consumed
     */
    private fun onFlingAnimateStep(): Boolean {
        var isScrolled = false
        val curTime = System.currentTimeMillis()
        val percentTime = (curTime - startTime).toFloat() / (endTime - startTime).toFloat()
        val percentDistance = flingAnimInterpolator.getInterpolation(percentTime)
        val curDx = percentDistance * totalAnimDx
        val curDy = percentDistance * totalAnimDy
        val distanceX = curDx - lastAnimDx
        val distanceY = curDy - lastAnimDy
        lastAnimDx = curDx
        lastAnimDy = curDy
        if (is2DScrollingEnabled) {
            isScrolled = scroll2D(-distanceX, -distanceY)
        } else if (isScrollingHorizontally) {
            isScrolled = scrollHorizontal(-distanceX)
        } else if (isScrollingVertically) {
            isScrolled = scrollVertical(-distanceY)
        }

        // This will stop fling animation if user has touch intercepted
        if (!isFlinging) {
            return false
        }
        if (percentTime < 1.0f) {
            // fling animation running
            post { onFlingAnimateStep() }
        } else {
            // fling animation ended
            isFlinging = false
            isScrollingVertically = false
            isScrollingHorizontally = false
        }
        return isScrolled
    }

    abstract fun onSingleTapUpEvent(e: MotionEvent)
    //endregion Scrolling, Flinging and Click events

    //region Scrolling methods
    /**
     * Scroll horizontally
     *
     * @param distanceX distance to scroll
     * @return true if horizontally scrolled, false otherwise
     */
    fun scrollHorizontal(distanceX: Float): Boolean {
        if (!canScrollHorizontally() || distanceX == 0f) {
            return false
        }
        var newScrolledLeft = scrolledContentRect.left - distanceX.toInt()
        var newScrolledRight = scrolledContentRect.right - distanceX.toInt()
        if (newScrolledLeft > 0) {
            newScrolledLeft = 0
            newScrolledRight = contentRect.right
        } else if (newScrolledLeft < -(contentRect.right - viewRect.right)) {
            newScrolledLeft = -(contentRect.right - viewRect.right)
            newScrolledRight = viewRect.right
        }
        if (scrolledContentRect.left == newScrolledLeft) {
            return false
        }
        scrolledContentRect.set(
            newScrolledLeft, scrolledContentRect.top, newScrolledRight, scrolledContentRect.bottom
        )
        invalidate()
        return true
    }

    /**
     * Scroll vertically
     *
     * @param distanceY distance to scroll
     * @return true if vertically scrolled, false otherwise
     */
    fun scrollVertical(distanceY: Float): Boolean {
        if (!canScrollVertically() || distanceY == 0f) {
            return false
        }
        var newScrolledTop = scrolledContentRect.top - distanceY.toInt()
        var newScrolledBottom = scrolledContentRect.bottom - distanceY.toInt()
        if (newScrolledTop > 0) {
            newScrolledTop = 0
            newScrolledBottom = contentRect.bottom
        } else if (newScrolledTop < -(contentRect.bottom - viewRect.bottom)) {
            newScrolledTop = -(contentRect.bottom - viewRect.bottom)
            newScrolledBottom = viewRect.bottom
        }
        if (scrolledContentRect.top == newScrolledTop) {
            return false
        }
        scrolledContentRect.set(
            scrolledContentRect.left, newScrolledTop, scrolledContentRect.right, newScrolledBottom
        )
        invalidate()
        return true
    }

    /**
     * Scroll vertically & horizontal both side
     *
     * @param distanceX distance to scroll
     * @param distanceY distance to scroll
     * @return true if scrolled, false otherwise
     */
    fun scroll2D(distanceX: Float, distanceY: Float): Boolean {
        var isScrollHappened = false
        var newScrolledLeft: Int
        var newScrolledTop: Int
        val newScrolledRight: Int
        val newScrolledBottom: Int
        if (canScrollHorizontally()) {
            newScrolledLeft = scrolledContentRect.left - distanceX.toInt()
            newScrolledRight = scrolledContentRect.right - distanceX.toInt()
            if (newScrolledLeft > 0) {
                newScrolledLeft = 0
            }
            if (newScrolledLeft < -(contentRect.right - viewRect.right)) {
                newScrolledLeft = -(contentRect.right - viewRect.right)
            }
            isScrollHappened = true
        } else {
            newScrolledLeft = scrolledContentRect.left
            newScrolledRight = scrolledContentRect.right
        }
        if (canScrollVertically()) {
            newScrolledTop = scrolledContentRect.top - distanceY.toInt()
            newScrolledBottom = scrolledContentRect.bottom - distanceY.toInt()
            if (newScrolledTop > 0) {
                newScrolledTop = 0
            }
            if (newScrolledTop < -(contentRect.bottom - viewRect.bottom)) {
                newScrolledTop = -(contentRect.bottom - viewRect.bottom)
            }
            isScrollHappened = true
        } else {
            newScrolledTop = scrolledContentRect.top
            newScrolledBottom = scrolledContentRect.bottom
        }
        if (!isScrollHappened) {
            return false
        }
        scrolledContentRect.set(
            newScrolledLeft, newScrolledTop, newScrolledRight, newScrolledBottom
        )
        invalidate()
        return true
    }

    /**
     * Check if content width is bigger than  view width
     *
     * @return true if content width is bigger than view width
     */
    fun canScrollHorizontally(): Boolean {
        return contentRect.right > viewRect.right
    }

    /**
     * Check if content height is bigger than  view height
     *
     * @return true if content height is bigger than view height
     */
    fun canScrollVertically(): Boolean {
        return contentRect.bottom > viewRect.bottom
    }

    /**
     * @return true if content are scrollable from top to bottom side
     */
    fun canScrollTop(): Boolean {
        return scrolledContentRect.top < viewRect.top
    }

    /**
     * @return true if content are scrollable from bottom to top side
     */
    fun canScrollBottom(): Boolean {
        return scrolledContentRect.bottom > viewRect.bottom
    }

    /**
     * @return true if content are scrollable from left to right side
     */
    fun canScrollRight(): Boolean {
        return scrolledContentRect.right > viewRect.right
    }

    /**
     * @return true if content are scrollable from right to left side
     */
    fun canScrollLeft(): Boolean {
        return scrolledContentRect.left < viewRect.left
    }
    //endregion Scrolling methods

    //region NestedScrollingChild implementation
    abstract val nestedScrollingChildHelper: NestedScrollingChildHelper
    private var nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE

    /**
     * Default Nested scroll axis is ViewCompat.SCROLL_AXIS_NONE.
     *
     * Nested scroll axis must be one of the
     * ViewCompat.SCROLL_AXIS_NONE,
     * ViewCompat.SCROLL_AXIS_HORIZONTAL or
     * ViewCompat.SCROLL_AXIS_VERTICAL
     *
     * @param nestedScrollAxis value of nested scroll direction
     */
    fun setNestedScrollAxis(@ViewCompat.ScrollAxis nestedScrollAxis: Int) {
        this.nestedScrollAxis = nestedScrollAxis
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return nestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        nestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun hasNestedScrollingParent(): Boolean {
        return nestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return nestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float, velocityY: Float, consumed: Boolean
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        nestedScrollingChildHelper.onDetachedFromWindow()
    }
    //endregion NestedScrollingChild implementation
}
