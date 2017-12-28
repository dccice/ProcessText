package com.example.process;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by ice on 2017/5/23.
 */

public class processBar extends View {
    private Paint mPaint = null;
    private int mProWidth; //进度条的宽度   
    private int mRectWidth; //用来显示文字矩形的宽度
    private int mProHeight; //进度条的高度
    private int mProColor; //进度条的颜色
    private int mRectColor; //显示文字矩形的颜色
    private Path mPath; //用来绘制底下的三角形
    private Rect mTextBound;  //计算文字的宽度和高度

    private int mRectLeftInit; //显示文字的矩形左上角的初始位置，绘制的时候需要动态改变
    private int mRectRightInit; //显示文字矩形右下角的初始位置，绘制的时候需要动态改变

    private int mTextLeftInit; //文字左边的坐标，需要动态改变


    private int mProgressRight; //显示进度条的矩形右边相对于当前view的坐标

    private int mRectangleBottomPoint; //三角形底部的x,y坐标
    private int mRectangleLeftPoint;  //三角形左上角点的坐标
    private int mRectangleRightPoint; //三角形右上角点的坐标
    private static final String TAG = "MyProBar";
    private String text ="0%";
    private Bitmap ic_launcher;


    public processBar(Context context) {
        this(context, null);
    }

    public processBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public processBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义的属性
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.self_pro, defStyleAttr, 0);
        try {
            int count = array.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = array.getIndex(i);
                switch (attr) {
                    case R.styleable.self_pro_pro_color:
                        mProColor = array.getColor(attr, Color.RED);
                        break;
                    case R.styleable.self_pro_rect_width:
                        mRectWidth = px2dip(context, array.getDimensionPixelSize(attr, 80));
                        break;
                    case R.styleable.self_pro_rect_color:
                        mRectColor = array.getColor(attr, Color.GREEN);
                        break;
                    case R.styleable.self_pro_pro_width:
                        mProWidth = px2dip(context, array.getDimensionPixelSize(attr, 260));
                        break;
                    case R.styleable.self_pro_pro_height:
                        mProHeight = px2dip(context, array.getDimensionPixelSize(attr, 10));
                        Log.d(TAG, "the mProHeight is :" + mProHeight);
                        break;
                    default:
                        break;
                }
            }
        } finally {
            //获取属性值完成之后，记得回收
            array.recycle();
        }
        init();
    }

    /**
     * px转换成dip，如果不转换，跟我们设置的值存在一定的偏差
     *
     * @param context
     * @param pxValue
     * @return
     */
    public int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 测量当前view的大小
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            //宽度是 mRectWidth / 2 + mProWidth + mProWidth / 2
            width = mProWidth + mRectWidth;
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            //height = 进度条的高度 + 矩形的高度 + 三角形的高度   ，这里我默认将三角形的边长= 矩形宽度 / 2 ，矩形高度 = 矩形宽度 / 2
            height = (int) (mProHeight + (mRectWidth / 2) * Math.cos(Math.PI / 6) + mRectWidth / 2);
            if (widthMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }
        Log.d(TAG, "the width in onmeasure is :" + width + "===the height in onmeasure is :" + height);
        //计算完成宽度和高度，记得调用setMeasuredDimension
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Log.d(TAG, "the width is :" + width + "===the height is :" + height);

        //绘制灰色的底部进度条轨迹
        mPaint.setColor(Color.parseColor("#dddddd"));
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(mRectWidth / 2, height - mProHeight, width - mRectWidth / 2, height), 5, 5, mPaint);

        //绘制进度条的进度，这里由于mRectWidth初始值=0,所以是看不到的
        mPaint.setColor(mProColor);
        canvas.drawRoundRect(new RectF(mRectWidth / 2, height - mProHeight, mProgressRight, height), 5, 5, mPaint);


        //设置当前的三角和矩形的颜色为获取的自定义属性的值
        mPaint.setColor(mRectColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        //这里记得调用reset方法，否则上一次绘制的三角形是不会消失的
        mPath.reset();
        //绘制三角形，这里的三角形三个点的x坐标会一直变化，y坐标则保持不变
//        mPath.moveTo(mRectangleBottomPoint, height - mProHeight);
//        mPath.lineTo(mRectangleLeftPoint, (float) (height - mProHeight - (mRectWidth / 2) * Math.cos(Math.PI / 6)));
//        mPath.lineTo(mRectangleRightPoint, (float) (height - mProHeight - (mRectWidth / 2) * Math.cos(Math.PI / 6)));
        //调用close()方法，自动将三个点连接起来
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        //绘制显示文字的矩形
//        canvas.drawRoundRect(new RectF(mRectLeftInit, 0, mRectRightInit, mRectWidth / 2), 3, 3, mPaint);
        //加10 是去掉三角形了  所以才加的10
        canvas.drawBitmap(ic_launcher,null,new RectF(mRectLeftInit, 10, mRectRightInit, mRectWidth / 2+10), mPaint);
        //重新设置颜色
        mPaint.setColor(Color.BLACK);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        //绘制百分比  这的加10一样的
        canvas.drawText(text, mTextLeftInit, mRectWidth / 4 + mTextBound.height() / 2+10, mPaint);
    }

    /**
     * 1. 初始化一些数值
     */
    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mTextBound = new Rect();
        mPath = new Path();

        mRectangleBottomPoint = mRectWidth / 2;
        mRectangleLeftPoint = mRectWidth / 4;
        mRectangleRightPoint = mRectWidth * 3 / 4;

        mPaint.getTextBounds(text, 0, text.length(), mTextBound);
        mTextLeftInit = mRectWidth / 2 - mTextBound.width() / 2;

        mRectRightInit = mRectWidth;

        mProgressRight = mRectWidth / 2;
        ic_launcher = getRes("icon");
    }
    public Bitmap getRes(String name) {
        ApplicationInfo appInfo = getContext().getApplicationInfo();
        int resID = getResources().getIdentifier(name, "mipmap", appInfo.packageName);
        return BitmapFactory.decodeResource(getResources(), resID);
    }
    /**
     * 设置百分比 1-100之内的数字
     * @param percnet
     */
    public void setPercent( int percnet) {
        if(percnet< 0 || percnet >100){
            return;
        }
        //换算百分比所要增加的长度
        double step = (mProWidth * 1.0) / 100 * percnet;
        //改变所有需要改变x轴坐标
        mRectangleBottomPoint += step;
        mRectangleLeftPoint += step;
        mRectangleRightPoint += step;
        mProgressRight += step;
        mTextLeftInit += step;
        mRectLeftInit += step;
        mRectRightInit += step;

        //如果当前包含文字的矩形左上角的坐标大于 + 本身的宽度　＞　真个view的宽度，重新设置其最大值
        if (mRectLeftInit + mRectWidth >= mProWidth + mRectWidth) {
            mRectLeftInit = mProWidth;
        }
        //如果当前包含文字的矩形右上角的x轴坐标　＞　真个view的宽度，重新设置其最大值
        if (mRectRightInit >= mProWidth + mRectWidth) {
            mRectRightInit = mProWidth + mRectWidth;
        }
        //对进度条的右下角x轴坐标进行限制
        if (mProgressRight >= mProWidth + mRectWidth / 2) {
            mProgressRight = mProWidth + mRectWidth / 2;
        }
        //限制三角形三个点的x轴坐标
        if (mRectangleBottomPoint >= mProWidth + mRectWidth / 2) {
            mRectangleBottomPoint = mProWidth + mRectWidth / 2;
            mRectangleLeftPoint = mRectangleBottomPoint - mRectWidth / 4;
            mRectangleRightPoint = mRectangleBottomPoint + mRectWidth / 4;
        }
        //计算当前进度的百分比
        int first = mRectangleBottomPoint - mRectWidth / 2;
        int percent = (int) Math.round(first / (mProWidth * 1.0) * 100);
        percent = percent >= 100 ? 100 : percent;
        Log.d("haha", "the first is :" + first + "====the second is :" + mProWidth + "====percent is :" + percent);
        text = percent + "%";
        //绘制当前进度内容
        mPaint.getTextBounds(text, 0, text.length(), mTextBound);
        if (mTextLeftInit >= (mProWidth + mRectWidth / 2 - mTextBound.width() / 2)) {
            mTextLeftInit = mProWidth + mRectWidth / 2 - mTextBound.width() / 2;
        }
        //重绘，这里是在新线程里，所以需要调用postInvalidate();
        postInvalidate();
        //判三角形底部的坐标如果 达到进度条末尾的x节点，说明绘制结束了。
        if( mRectangleBottomPoint == mProWidth + mRectWidth / 2) {
            return;
        }
    }

}
