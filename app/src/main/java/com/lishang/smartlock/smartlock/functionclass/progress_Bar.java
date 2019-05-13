package com.lishang.smartlock.smartlock.functionclass;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import com.lishang.smartlock.R;

public class progress_Bar extends ProgressBar {
    private static final int DEFAULT_TEXT_SIZE = 10;
    private static final int DEFAULT_TEXT_COLOR = 0xFFFC00D1;
    private static final int DEFAULT_COLOR_UNREACH = 0XFFD3D6DA;
    private static final int DEFAULT_HEIGHT_UNREACH = 2;//dp
    private static final int DEFAULT_COLOR_REACH = DEFAULT_TEXT_COLOR;
    private static final int DEFAULT_HEIGHT_REACH = 2;//dp
    private static final int DEFAULT_TEXT_OFFSET = 10;//dp
private int mTextSize = sp2px(DEFAULT_TEXT_SIZE);
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mUnReachColor = DEFAULT_COLOR_UNREACH;
    private int mUnReachHeight = sp2px(DEFAULT_HEIGHT_UNREACH);
    private int mReachColor = DEFAULT_COLOR_REACH;
    private int mReachHeight = sp2px(DEFAULT_HEIGHT_REACH);
    private int mTextOffSet = sp2px(DEFAULT_TEXT_OFFSET);
private Paint mpaint = new Paint();

private int mRealWidth;

    public progress_Bar( Context context ) {
        this(context, null);
    }

    public progress_Bar( Context context, AttributeSet attrs ) {
        this(context, attrs,0);

    }

    public progress_Bar( Context context, AttributeSet attrs, int defStyleAttr ) {
        super(context, attrs, defStyleAttr);
        obtaimStyleAttrs(attrs);
    }
//获取自定义属性
    private void obtaimStyleAttrs( AttributeSet attrs ) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs,
                R.styleable.progress_Bar);
        mTextSize = (int) ta.getDimension(R.styleable.progress_Bar_progress_text_size,mTextSize);
        mTextColor = (int) ta.getDimension(R.styleable.progress_Bar_progress_text_color,mTextColor);
        mUnReachColor = (int) ta.getDimension(R.styleable.progress_Bar_progress_unreach_color,mTextSize);
        mUnReachHeight = (int) ta.getDimension(R.styleable.progress_Bar_progress_unreach_height,mUnReachHeight);
        mReachColor = (int) ta.getDimension(R.styleable.progress_Bar_progress_reach_color,mReachColor);
        mReachHeight = (int) ta.getDimension(R.styleable.progress_Bar_progress_reach_height,mReachHeight);
        mTextOffSet = (int) ta.getDimension(R.styleable.progress_Bar_progress_text_offset,mTextOffSet);
ta.recycle();
mpaint.setTextSize(mTextSize);
    }

    @Override
    protected synchronized void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(widthVal,height);
        mRealWidth = getMeasuredWidth() -getPaddingLeft() - getPaddingRight();
    }
//三种模式
    private int measureHeight( int heightMeasureSpec ) {
        int result = 0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if(mode == MeasureSpec.EXACTLY){
            result = size;
        }else{
            int textHeight = (int) (mpaint.descent() - mpaint.ascent());
            result = getPaddingTop()
                    + getPaddingBottom()
                    + Math.max(Math.max(mReachHeight,mUnReachHeight)
                    , Math.abs(textHeight));
            if (mode == MeasureSpec.AT_MOST){
                result = Math.min(result,size);
            }
        }
        return result;
    }

    @Override
    protected synchronized void onDraw( Canvas canvas ) {
        canvas.save();
        canvas.translate(getPaddingLeft(),getHeight()/2);
        boolean noNeedUnReach = false;
        //draw reach bar
        String text = getProgress() + "%";
        int textWidth = (int) mpaint.measureText(text);
        float radio = getProgress()*1.0f /getMax();
        float progressX = radio * mRealWidth;
        float endX = progressX - mTextOffSet/2;
        if(progressX + textWidth > mRealWidth){
            progressX = mRealWidth - textWidth;
            noNeedUnReach = true;
        }
        if(endX > 0) {
        mpaint.setColor(mReachColor);
        mpaint.setStrokeWidth(mRealWidth);
        canvas.drawLine(0,0,endX,0,mpaint);
        }
        //draw text
        mpaint.setColor(mTextColor);
        int y = (int) -((mpaint.descent() + mpaint.ascent())/2);
        canvas.drawText(text,progressX,y,mpaint);
        //draw unreach bar
        if(!noNeedUnReach){
            float start = progressX + mTextOffSet/2 + textWidth;
            mpaint.setColor(mUnReachColor);
            mpaint.setStrokeWidth(mUnReachHeight);
            canvas.drawLine(start,0,mRealWidth,0,mpaint);
        }
        canvas.restore();
    }

    private int dp2px( int dpVal ) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                getResources().getDisplayMetrics());
    }

    private int sp2px( int spVal ) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal,
                getResources().getDisplayMetrics());
    }
}
