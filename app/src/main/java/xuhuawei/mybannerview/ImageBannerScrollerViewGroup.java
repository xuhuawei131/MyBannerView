package xuhuawei.mybannerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Administrator on 2018/5/5 0005.
 * 我们自定义ViewGroup布局的时候
 * 必须要实现的方法：测量-》布局-》绘制
 */
public class ImageBannerScrollerViewGroup extends ViewGroup {

    private int lastX;
    private int lastY;

    private int x;

    private int subWidth;
    private int subHeight;

    private int index;


    private Scroller mScroller;
    public ImageBannerScrollerViewGroup(Context context) {
        super(context);
        init(null);
    }

    public ImageBannerScrollerViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ImageBannerScrollerViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mScroller=new Scroller(getContext());
    }

    /**
     * 我们想要测量viewgroup的宽高，就必须先测量子视图的宽高
     * 1、先求出 字视图的个数  int childCount=getChildCount();
     * 2、测量子视图的宽高
     * 3、根据子视图的宽高 求出该视图的宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        //如果没有子视图 那么就不显示
        if (childCount < 1) {
            setMeasuredDimension(0, 0);
        } else {
            //测量子视图的宽高 这里是 估算子视图的宽高
            measureChildren(widthMeasureSpec, heightMeasureSpec);

            //我们使用第一个视图的高度作为视图的高度，第一个子视图的宽度*子视图的个数  作为视图的宽度
            View childView = getChildAt(0);
            subHeight = childView.getMeasuredWidth();
            subWidth = childView.getMeasuredHeight();
            int width = subWidth * childCount;
            setMeasuredDimension(width, subHeight);
        }
    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b) {
        if (change) {
            int childCount = getChildCount();
            int leftMargin = 0;
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                leftMargin = i * subWidth;
                view.layout(leftMargin, 0, leftMargin + subWidth, subHeight);
            }
        }
    }

    /**
     * @param ev
     * @return 如果返回为true  那么我们的viewgroup就会处理拦截事件 如果为false 那么我们的viewgroup将不会接受这个事件处理
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int delatX=x-lastX;
                int delatY=y-lastY;
                if (Math.abs(delatX)>Math.abs(delatY)){
                    isIntercept=true;
                }else{
                    isIntercept=false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                lastX=x;
                lastY=y;
                break;
        }
        return true;
    }

    /**
     * 两种方式实现我们的手动轮播
     * 1、利用scrollTo scrollBy 完成轮播
     * 2、利用Scroller 对象 完成轮播
     *
     *  第一、
     *  我们在完成滑动屏幕图片的过程中 其实就是我们自定义ViewGroup的在子视图的移动过程
     *  那么我们只需要知道滑动之前的横坐标和滑动之后的横坐标  此时我们就可以 求出我们过程中的距离
     *  我们再利用scrollBy方法实现图片的滚动  所以我们需要2个值 移动之前和移动之后横坐标
     *
     *  第二、
     *  在我们第一次按下的那一瞬间，此时的移动之前和移动之后的值是相等的，也就是 我们此时按下那一瞬间 的那一个点的横坐标
     *  第三、
     *  我们在不断的滑动过程中
     *
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x= (int) ev.getX();
                if (!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX= (int) ev.getX();
                int distance=moveX-x;
                scrollBy(-distance,0);
                x=moveX;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                int scrollX=getScrollX();
                index=(scrollX+subWidth/2)/subWidth;
                if (index<0){//此时已经滑动到了最左边
                    index=0;
                }else if(index>(getChildCount()-1)){//此时已经超过了最右边一张图
                    index=getChildCount()-1;
                }
//                scrollTo(index*subWidth,0);

                int dx=index*subWidth-scrollX;
                mScroller.startScroll(scrollX,0,dx,0);
                postInvalidate();
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),0);
            invalidate();
        }
    }
}
