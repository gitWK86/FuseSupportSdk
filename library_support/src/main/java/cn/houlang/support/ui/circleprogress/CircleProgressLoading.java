package cn.houlang.support.ui.circleprogress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

/**
 * Created by #Suyghur, on 2019/07/17.
 * Description :
 */
public class CircleProgressLoading extends View {

    /**
     * 圆的个数
     */
    public static final String NUM_OF_CIRCLES = "NUM_OF_CIRCLES";
    /**
     * 圆最大半径
     */
    public static final String MAX_RADIUS = "MAX_RADIUS";
    /**
     * 圆最小半径
     */
    public static final String MIN_RADIUS = "MIN_RADIUS";
    /**
     * 旋转速度
     */
    public static final String ROTATE_SPEED_IN_MILLIS = "ROTATE_SPEED_IN_MILLIS";
    /**
     * 是否顺时针
     */
    public static final String IS_CLOCKWISE = "IS_CLOCKWISE";
    /**
     * 圆的ARGB颜色
     */
    public static final String CIRCLE_COLOR = "CIRCLE_COLOR";
    /**
     * 偏移量
     */
    public static final String OFFSET = "OFFSET";

    int numOfCircles;
    int maxRadius;
    int minRadius;
    int rotateSpeedInMillis;
    int offset;
    boolean isClockwise;
    int[] circleColorArr;

    Paint mPaint;

    float rotateDegrees;
    int numOfRotate;

    public CircleProgressLoading(Context context) {
        super(context);
    }

    public void setParams(Bundle bundle) {
        if (bundle != null && bundle.containsKey(NUM_OF_CIRCLES)) {
            numOfCircles = bundle.getInt(NUM_OF_CIRCLES);
        } else {
            numOfCircles = 7;
        }
        if (bundle != null && bundle.containsKey(MAX_RADIUS)) {
            maxRadius = bundle.getInt(MAX_RADIUS);
        } else {
            maxRadius = 16;
        }
        if (bundle != null && bundle.containsKey(MIN_RADIUS)) {
            minRadius = bundle.getInt(MIN_RADIUS);
        } else {
            minRadius = 1;
        }
        if (bundle != null && bundle.containsKey(ROTATE_SPEED_IN_MILLIS)) {
            rotateSpeedInMillis = bundle.getInt(ROTATE_SPEED_IN_MILLIS);
        } else {
            rotateSpeedInMillis = 150;
        }
        if (bundle != null && bundle.containsKey(IS_CLOCKWISE)) {
            isClockwise = bundle.getBoolean(IS_CLOCKWISE);
        } else {
            isClockwise = true;
        }
        if (bundle != null && bundle.containsKey(OFFSET)) {
            offset = bundle.getInt(OFFSET);
        } else {
            offset = 3;
        }
        if (bundle != null && bundle.containsKey(CIRCLE_COLOR)) {
            circleColorArr = bundle.getIntArray(CIRCLE_COLOR);
        } else {
            circleColorArr = new int[]{255, 255, 255, 255};
        }
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.argb(circleColorArr[0], circleColorArr[1], circleColorArr[2], circleColorArr[3]));


        rotateDegrees = 360 / numOfCircles;
        numOfRotate = 0;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (numOfRotate == numOfCircles) {
            numOfRotate = 0;
        }

        if (isClockwise) {
            canvas.rotate(rotateDegrees * numOfRotate, getWidth() / 2, getHeight() / 2);
        } else {
            canvas.rotate(-rotateDegrees * numOfRotate, getWidth() / 2, getHeight() / 2);
        }

        //记录旋转的次数，下次invalidat()重绘时就可以使用新的角度旋转canvas，使小圆产生旋转的感觉
        numOfRotate++;

        //取View最短边，并减去最大圆的半径，得到所有圆所在的圆路径的半径
        int radius = (getWidth() > getHeight() ? getHeight() : getWidth()) / 2 - maxRadius;
        //每个小圆的半径增量
        float radiusIncrement = (float) (maxRadius - minRadius) / numOfCircles;
        //每隔多少度绘制一个小圆,弧度制
        double angle = 2 * Math.PI / numOfCircles;

        //按位置画小圆
        //每个小圆的位置可以由正弦余弦函数求出，并且每个小圆半径依次递增，若反方向则依次递减
        if (isClockwise) {
            for (int i = 0; i < numOfCircles; i++) {
                float x = (float) (getWidth() / 2 + Math.cos(i * angle) * (radius - offset * i));
                float y = (float) (getHeight() / 2 - Math.sin(i * angle) * (radius - offset * i));
                canvas.drawCircle(x, y, maxRadius - radiusIncrement * i, mPaint);
            }
        } else {
            for (int i = 0; i < numOfCircles; i++) {
                float x = (float) (getWidth() / 2 + Math.cos(i * angle) * (radius - offset * i));
                float y = (float) (getHeight() / 2 - Math.sin(i * angle) * (radius - offset * i));
                canvas.drawCircle(x, y, minRadius + radiusIncrement * i, mPaint);
            }
        }


        postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        }, rotateSpeedInMillis);
    }
}
