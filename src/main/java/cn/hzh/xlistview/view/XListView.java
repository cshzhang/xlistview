package cn.hzh.xlistview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import cn.hzh.xlistview.R;

/**
 * Created by hzh on 2015/10/29.
 */
public class XListView extends ListView implements AbsListView.OnScrollListener
{
    //scroll时间间隔，duration
    private static final int SCROLL_DURATION = 300;
    //比例值，有下拉的感觉
    private static final float OFFSET_RATIO = 1.8f;

    //LinearLayout
    private XHeaderView mHeaderView;
    //对应布局文件
    private RelativeLayout mHeaderViewContent;

    private OnScrollListener mScrollLitener;
    private XListViewRefreshListener mRefreshListener;
    //头部view的高度
    private int mHeaderHeight;
    //用Scroll对象将headerview移到正确位置
    private Scroller mScroller;
    //上一次的y值，在onTouchEvent中使用
    private float mLastY = -1;
    //是否正在刷新
    private boolean isPullRefreshing;
    //允许下拉刷新的开关
    private boolean isEnablePullRefresh = true;

    private boolean mUpdateTimeOnce;

    public XListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initView(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        if (mLastY == -1)
        {
            mLastY = ev.getY();
        }

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = ev.getY() - mLastY;
                mLastY = ev.getY();

                if (getFirstVisiblePosition() == 0 &&
                        (mHeaderView.getVisiableHeight() > 0 || deltaY > 0))
                {
                    if(isEnablePullRefresh)
                    {
                        //首先设置上次更新时间的TextView，而且只设置一次
                        if(!mUpdateTimeOnce)
                        {
                            mUpdateTimeOnce = true;
                            //更新上次更新时间的TextView
                            mHeaderView.updateTimeTextView(System.currentTimeMillis());
                        }
                        //除以一个ratio，是为了让用户有pull的感觉
                        updateHeaderHeightAndState(deltaY / OFFSET_RATIO);
                    }
                }

                break;
            default:
                mUpdateTimeOnce = false;
                mLastY = -1;

                if(getFirstVisiblePosition() == 0)
                {
                    startOnRefresh();
                    resetHeaderHeight();
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll()
    {
        //返回true，代表scrolling动画还没有结束
        if(mScroller.computeScrollOffset())
        {
            //mScroller.getCurrY()得到的值是相对于startY的值
            mHeaderView.setVisiableHeight(mScroller.getCurrY());
            invalidate();
        }
        super.computeScroll();
    }

    private void updateHeaderHeightAndState(float deltaY)
    {
        mHeaderView.setVisiableHeight((int) deltaY + mHeaderView.getVisiableHeight());
        if(!isPullRefreshing)
        {
            if (mHeaderView.getVisiableHeight() > mHeaderHeight)
            {
                mHeaderView.setState(XHeaderView.STATE_READY);
            }else
            {
                mHeaderView.setState(XHeaderView.STATE_NORMAL);
            }
        }
        setSelection(0);
    }

    private void initView(Context context)
    {
        mScroller = new Scroller(context, new DecelerateInterpolator());

        mHeaderView = new XHeaderView(context);
        mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.id_header_content);
        addHeaderView(mHeaderView, null, false);

        //init headerview height
        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                mHeaderHeight = mHeaderView.getHeight();
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        disablePullRefresh();

        setOnScrollListener(this);
    }

    private void resetHeaderHeight()
    {
        int height = mHeaderView.getVisiableHeight();
        if(height == 0)
            return;
        if(isPullRefreshing && height <= mHeaderHeight)
            return;

        int finalHeight = 0;
        if(isPullRefreshing && height > mHeaderHeight)
        {
            finalHeight = mHeaderHeight;
        }
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);

        //触发，computeScroll()
        invalidate();
    }

    private void startOnRefresh()
    {
        if(!isPullRefreshing && isEnablePullRefresh &&
                mHeaderView.getState() == XHeaderView.STATE_READY)
        {
            isPullRefreshing = true;
            mHeaderView.setState(XHeaderView.STATE_REFRESHING);

            //外部执行具体刷新操作
            if(mRefreshListener != null)
            {
                mRefreshListener.onRefresh();
            }
        }
    }

    /**
     * 手动刷新
     */
    public void startRefresh()
    {
        isPullRefreshing = true;
        mHeaderView.setState(XHeaderView.STATE_REFRESHING);

        if(mRefreshListener != null)
        {
            mRefreshListener.onRefresh();
        }
    }

    public void stopRefresh()
    {
        if(isPullRefreshing)
        {
            mUpdateTimeOnce = false;
            isPullRefreshing = false;
            resetHeaderHeight();

            //记录更新时间
            mHeaderView.setUpdateTime(System.currentTimeMillis());
        }
    }

    public void enablePullRefresh(XListViewRefreshListener listener)
    {
        isEnablePullRefresh = true;
        this.mRefreshListener = listener;
        mHeaderViewContent.setVisibility(VISIBLE);
    }

    public void disablePullRefresh()
    {
        isEnablePullRefresh = false;
        mHeaderViewContent.setVisibility(GONE);
    }

    public void setOnScrollLitener(OnScrollListener listener)
    {
        this.mScrollLitener = listener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if(mScrollLitener != null)
        {
            mScrollLitener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount)
    {
        if(mScrollLitener != null)
        {
            mScrollLitener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
        }
    }

    //刷新时，对外接口
    public interface XListViewRefreshListener
    {
        public void onRefresh();
    }
}
