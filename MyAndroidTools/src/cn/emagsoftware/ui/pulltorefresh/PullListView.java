package cn.emagsoftware.ui.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * Created by Wendell on 14-11-8.
 */
public class PullListView extends ListView {

    private static final float OFFSET_RADIO = 1.8f;

    private View mPullView = null;
    private Integer mPullViewHeight = null;
    private float mLastY = -1;
    private Scroller mScroller;
    private OnPullListener mOnPullListener = null;
    private int mState = 0; //0:normal,1:begin pull,2:ready,3:refreshing

    public PullListView(Context context) {
        super(context);
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }

    public PullListView(Context context, AttributeSet attrs) {
        super(context,attrs);
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }

    public PullListView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }

    public void addPullView(View v) {
        if(getHeaderViewsCount() > 0) throw new IllegalStateException("addPullView can be called only once and before addHeaderView.");
        mPullView = v;
        mPullView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPullViewHeight = mPullView.getHeight();
                if(mState == 0) updatePullViewHeight(0);
                mPullView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        LinearLayout wrap = new LinearLayout(getContext());
        wrap.addView(mPullView);
        wrap.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        addHeaderView(wrap,null,false);
    }

    private void updatePullViewHeight(int height) {
        if(height < 0) height = 0;
        ViewGroup.LayoutParams layoutParams = mPullView.getLayoutParams();
        if(layoutParams == null) layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,height);
        else layoutParams.height = height;
        mPullView.setLayoutParams(layoutParams);
    }

    @Override
    public boolean removeHeaderView(View v) {
        if(mPullView != null && mPullView.getParent() == v) throw new UnsupportedOperationException("current header view is pull view,could not be removed.");
        return super.removeHeaderView(v);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mLastY == -1) {
                    mLastY = ev.getRawY();
                }else {
                    final float deltaY = ev.getRawY() - mLastY;
                    mLastY = ev.getRawY();
                    if(mPullViewHeight != null) {
                        if(getFirstVisiblePosition() == 0) {
                            int curHeight = (int)(deltaY/OFFSET_RADIO + mPullView.getHeight());
                            if(deltaY > 0) {
                                updatePullViewHeight(curHeight);
                                checkState(curHeight,true);
                            }else if(deltaY < 0) {
                                if(mState == 3) {
                                    if(curHeight >= mPullViewHeight) {
                                        updatePullViewHeight(curHeight);
                                        checkState(curHeight,false);
                                        setSelection(0); // ����super.onTouchEvent(ev)��Ӱ��
                                    }
                                }else if(curHeight >= 0) {
                                    updatePullViewHeight(curHeight);
                                    checkState(curHeight,false);
                                    setSelection(0); // ����super.onTouchEvent(ev)��Ӱ��
                                }
                            }
                        }
                    }
                }
                break;
            default:
                mLastY = -1;
                if(mPullViewHeight != null) {
                    if(mState != 0) {
                        if(getFirstVisiblePosition() == 0) {
                            int curHeight = mPullView.getHeight();
                            if(mState == 3) {
                                mScroller.startScroll(0, curHeight, 0, mPullViewHeight - curHeight, 400);
                                invalidate();
                            }else {
                                int finalHeight = 0;
                                if(mState == 2) {
                                    mState = 3;
                                    if(mOnPullListener != null) mOnPullListener.onRefreshing(mPullView);
                                    finalHeight = mPullViewHeight;
                                }
                                mScroller.startScroll(0, curHeight, 0, finalHeight - curHeight, 400);
                                invalidate();
                            }
                        }else {
                            if(mState == 3) updatePullViewHeight(mPullViewHeight);
                            else updatePullViewHeight(0);
                        }
                        if(mState != 3) {
                            mState = 0;
                            if(mOnPullListener != null) mOnPullListener.onCanceled(mPullView);
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void checkState(int curHeight,boolean isIncreased) {
        if(mState != 3) {
            if(curHeight > mPullViewHeight) {
                if(mState != 2) {
                    mState = 2;
                    if(mOnPullListener != null) mOnPullListener.onReady(mPullView);
                }
            }else {
                if(mState != 1) {
                    mState = 1;
                    if(mOnPullListener != null) mOnPullListener.onBeginPull(mPullView);
                }
            }
            if(mOnPullListener != null) mOnPullListener.onScroll(mPullView,(float)curHeight/mPullViewHeight,isIncreased);
        }
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            updatePullViewHeight(mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    public void setOnPullListener(OnPullListener listener) {
        this.mOnPullListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if(mPullView == null) throw new IllegalStateException("pull view is null,call addPullView first.");
        if(refreshing) {
            if(mState != 3) {
                mState = 3;
                if(mOnPullListener != null) mOnPullListener.onRefreshing(mPullView);
                if(mPullViewHeight != null) {
                    int curHeight = mPullView.getHeight();
                    mScroller.startScroll(0, curHeight, 0, mPullViewHeight - curHeight, 400);
                    invalidate();
                }
                setSelection(0);
            }
        }else {
            if(mState == 3) {
                mState = 0;
                if(mPullViewHeight != null) {
                    int curHeight = mPullView.getHeight();
                    mScroller.startScroll(0, curHeight, 0, -curHeight, 400);
                    invalidate();
                }
            }
        }
    }

}
