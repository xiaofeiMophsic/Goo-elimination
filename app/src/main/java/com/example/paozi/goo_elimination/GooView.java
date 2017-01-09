package com.example.paozi.goo_elimination;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * 作者：xiaofei
 * 日期：2017/1/9
 */

public class GooView extends View {

    private Paint mPaint;
    // A 小圆， B 大圆
    private float circleACenter = 500f;
    private float circleARadius = 60f;
    private float circleBCenter = 500f;
    private float circleBRadius = 70f;

    private float mMaxDistance = 1000f;
    private boolean isOutRange;
    private boolean isDisappear;
    private PointF[] circleAPfs = new PointF[2];
    private PointF[] circleBPfs = new PointF[2];

    private PointF mControlPoint = new PointF();
    private PointF circleACenterPoint = new PointF(circleACenter, circleACenter);
    private PointF circleBCenterPoint = new PointF(circleBCenter, circleBCenter);

    public GooView(Context context) {
        this(context, null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpce = MeasureSpec.getMode(widthMeasureSpec);

        int result = 200;

        switch (widthSpce) {
            case MeasureSpec.EXACTLY:
                result = width;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                result = Math.min(width, 200);
                break;
        }
        setMeasuredDimension(result, result);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float distance = (float) Math.sqrt(Math.pow((circleACenterPoint.x - circleBCenterPoint.x), 2) + Math.pow((circleACenterPoint.y - circleBCenterPoint.y), 2));
        distance = Math.min(distance, mMaxDistance);

        float percent = distance / mMaxDistance;
        float circleATempRadius = ((Number) circleARadius).floatValue() + percent * (((Number) (circleARadius * 0.2f)).floatValue() - ((Number) circleARadius).floatValue());

        float xOffset = circleACenterPoint.x - circleBCenterPoint.x;
        float yOffset = circleACenterPoint.y - circleACenterPoint.y;

        double lineK = 0.0;
        if (xOffset != 0) {
            lineK = yOffset / xOffset;
        }

        circleBPfs = getPoints(circleBCenterPoint, circleBRadius, lineK);
        circleAPfs = getPoints(circleACenterPoint, circleATempRadius, lineK);

        mControlPoint = new PointF((circleACenterPoint.x + circleBCenterPoint.x) / 2, (circleACenterPoint.y + circleBCenterPoint.y) / 2);

        canvas.save();
        if (!isDisappear) {
            if (!isOutRange) {
                Path path = new Path();
                path.moveTo(circleAPfs[0].x, circleAPfs[0].y);
                path.quadTo(mControlPoint.x, mControlPoint.y, circleBPfs[0].x, circleBPfs[0].y);
                path.lineTo(circleBPfs[1].x, circleBPfs[1].y);
                path.quadTo(mControlPoint.x, mControlPoint.y, circleAPfs[1].x, circleAPfs[1].y);

                canvas.drawPath(path, mPaint);
                canvas.drawCircle(circleACenterPoint.x, circleACenterPoint.y, circleATempRadius, mPaint);
            }

            canvas.drawCircle(circleBCenterPoint.x, circleBCenterPoint.y, circleBRadius, mPaint);
        }
        canvas.restore();
    }

    public static PointF[] getPoints(PointF middle, float radius, double lineK) {
        PointF[] ps = new PointF[2];

        float radian, xOffset = 0, yOffset = 0;
        if (lineK != 0) {
            radian = (float) Math.atan(lineK);
            xOffset = (float) (Math.sin(radian) * radius);
            yOffset = (float) (Math.cos(radian) * radius);
        } else {
            xOffset = radius;
            yOffset = 0;
        }

        ps[0] = new PointF(middle.x - xOffset, middle.y - yOffset);
        ps[1] = new PointF(middle.x + xOffset, middle.y + yOffset);

        return ps;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float downX;
        float downY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isOutRange = false;
                isDisappear = false;
                downX = event.getX();
                downY = event.getY();
                circleBCenterPoint.set(downX, downY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                downX = event.getX();
                downY = event.getY();
                circleBCenterPoint.set(downX, downY);
                invalidate();

                float distance = (float) Math.sqrt(Math.pow((circleACenterPoint.x - circleBCenterPoint.x), 2) +
                        Math.pow((circleACenterPoint.y - circleBCenterPoint.y), 2));
                if (distance > mMaxDistance) {
                    isOutRange = true;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:

                if (isOutRange) {
                    distance = (float) Math.sqrt(Math.pow((circleACenterPoint.x - circleBCenterPoint.x), 2) +
                            Math.pow((circleACenterPoint.y - circleBCenterPoint.y), 2));
                    if (distance > mMaxDistance) {
                        isDisappear = true;
                        invalidate();
                    } else {
                        circleBCenterPoint.set(circleACenterPoint.x, circleACenterPoint.y);
                        invalidate();
                    }
                } else {
                    final PointF temp = new PointF(circleBCenterPoint.x, circleBCenterPoint.y);
                    ValueAnimator animator = ValueAnimator.ofFloat(1.0f);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float percent = animation.getAnimatedFraction();
                            PointF pointf = new PointF(((Number) temp.x).floatValue() + ((Number) circleACenterPoint.x).floatValue() - percent * ((Number) temp.x).floatValue(),
                                    ((Number) temp.y).floatValue() + ((Number) circleACenterPoint.y).floatValue() - percent * ((Number) temp.y).floatValue());
                            circleBCenterPoint.set(pointf);
                            invalidate();
                        }
                    });
                    animator.setInterpolator(new OvershootInterpolator(4));
                    animator.setDuration(500);
                    animator.start();
                }
                break;
        }
        return true;
    }
}
