package cn.hzh.xlistview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import cn.hzh.xlistview.R;

/**
 * Created by hzh on 2015/10/29.
 */
public class XHeaderView extends LinearLayout
{
    private RelativeLayout mHeaderContent;

    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;
    private TextView mHintTextView;
    private TextView mLastUpdateTextView;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    public final static int  ANIMATION_DURATION = 500;

    public long mLastUpdateTime = 0;
    //单位毫秒
    public final static long ONE_MINITE = 60 * 1000;
    public final static long ONE_HOUR = ONE_MINITE * 60;
    public final static long ONE_DAY = ONE_HOUR * 24;

    /**
     * 状态值
     */
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;
    private int mState = STATE_NORMAL;

    public XHeaderView(Context context)
    {
        super(context);
        initView(context);
    }

    public XHeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        mHeaderContent = (RelativeLayout) inflater.inflate(R.layout.header_lv, this, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mHeaderContent, lp);
        setGravity(Gravity.BOTTOM);

        mArrowImageView = (ImageView) mHeaderContent.findViewById(R.id.id_arrow_iv);
        mProgressBar = (ProgressBar) mHeaderContent.findViewById(R.id.id_progress);
        mHintTextView = (TextView) mHeaderContent.findViewById(R.id.id_refresh_tip_tv);
        mLastUpdateTextView = (TextView) mHeaderContent.findViewById(R.id.id_last_update_tv);

        //初始化anim
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ANIMATION_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ANIMATION_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    public void setVisiableHeight(int height)
    {
        if(height < 0)
            height = 0;

        LinearLayout.LayoutParams lp = (LayoutParams) mHeaderContent.getLayoutParams();
        lp.height = height;
        mHeaderContent.setLayoutParams(lp);
    }

    public int getVisiableHeight()
    {
        LinearLayout.LayoutParams lp = (LayoutParams) mHeaderContent.getLayoutParams();
        return lp.height;
    }

    public void setState(int state)
    {
        if(state == mState)
            return;

        if(state == STATE_REFRESHING)
        {
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(INVISIBLE);
            mProgressBar.setVisibility(VISIBLE);
        }else
        {
            mArrowImageView.setVisibility(VISIBLE);
            mProgressBar.setVisibility(INVISIBLE);
        }

        switch (state)
        {
            case STATE_NORMAL:
                if(mState == STATE_READY)
                {
                    mArrowImageView.startAnimation(mRotateDownAnim);
                }
                if(mState == STATE_REFRESHING)
                {
                    mArrowImageView.clearAnimation();
                }

                mHintTextView.setText(R.string.pull_to_refresh);
                break;
            case STATE_READY:
                if(mState != STATE_READY)
                {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(mRotateUpAnim);
                    mHintTextView.setText(R.string.release_to_refresh);
                }
                break;
            case STATE_REFRESHING:
                mHintTextView.setText(R.string.refreshing);
                break;
        }

        mState = state;
    }

    public int getState()
    {
        return mState;
    }

    public void updateTimeTextView(long updateTime)
    {
        long curUpdateTime = updateTime;

        if(mLastUpdateTime == 0)
        {
            mLastUpdateTextView.setText(R.string.update_just_now);
            return;
        }

        String lastUpdate = "上次更新：";
        if(curUpdateTime - mLastUpdateTime < ONE_MINITE)
        {
            //刚刚更新
            mLastUpdateTextView.setText(R.string.update_just_now);
        }else if(curUpdateTime - mLastUpdateTime < ONE_HOUR)
        {
            int minite = (int) ((curUpdateTime - mLastUpdateTime) / 1000 /60);
            mLastUpdateTextView.setText(lastUpdate + minite + "分钟");
        }else if(curUpdateTime - mLastUpdateTime < ONE_DAY)
        {
            int hour = (int) ((curUpdateTime - mLastUpdateTime) / 1000 /60 / 60);
            mLastUpdateTextView.setText(lastUpdate + hour + "小时");
        }
    }

    public long getLastUpdateTime()
    {
        return mLastUpdateTime;
    }

    public void setUpdateTime(long updateTime)
    {
        this.mLastUpdateTime = updateTime;
    }
}
