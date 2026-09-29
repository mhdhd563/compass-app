package com.baidumap.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {

    private Paint basePaint;
    private Paint circlePaint;
    private Paint tickPaint;
    private Paint tickPaintMajor;
    private Paint dirPaint;
    private Paint needlePaint;
    private Paint centerPaint;
    private Paint innerDotPaint;
    private float degree = 0f;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(Color.parseColor("#0DFFFFFF"));

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#3DFFFFFF"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(2f);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#59FFFFFF"));
        tickPaint.setStrokeWidth(2f);

        tickPaintMajor = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaintMajor.setColor(Color.parseColor("#A6FFFFFF"));
        tickPaintMajor.setStrokeWidth(3f);

        dirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dirPaint.setColor(Color.WHITE);
        dirPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.parseColor("#4FC3F7"));

        innerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerDotPaint.setColor(Color.parseColor("#0B1026"));
    }

    public void setDegree(float deg) {
        this.degree = deg;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - 20f;

        canvas.drawCircle(cx, cy, radius, basePaint);
        canvas.drawCircle(cx, cy, radius, circlePaint);
        canvas.drawCircle(cx, cy, radius - 10f, circlePaint);

        for (int i = 0; i < 360; i += 5) {
            float rad = (float) Math.toRadians(i - 90);
            boolean major = (i % 30 == 0);
            float innerR = major ? radius - 30f : radius - 18f;
            float outerR = radius - 8f;
            canvas.drawLine(
                cx + innerR * (float) Math.cos(rad),
                cy + innerR * (float) Math.sin(rad),
                cx + outerR * (float) Math.cos(rad),
                cy + outerR * (float) Math.sin(rad),
                major ? tickPaintMajor : tickPaint
            );
        }

        canvas.save();
        canvas.rotate(-degree, cx, cy);

        float textSize = radius * 0.16f;
        dirPaint.setTextSize(textSize);

        String[] dirs = {"N", "E", "S", "W"};
        float markerR = radius - 50f;
        for (int i = 0; i < 4; i++) {
            float rad = (float) Math.toRadians(i * 90 - 90);
            float tx = cx + markerR * (float) Math.cos(rad);
            float ty = cy + markerR * (float) Math.sin(rad) + textSize / 3f;
            dirPaint.setColor(dirs[i].equals("N") ? Color.parseColor("#FF5252") : Color.WHITE);
            canvas.drawText(dirs[i], tx, ty, dirPaint);
        }

        float needleR = radius - 40f;
        float needleHalfW = radius * 0.055f;

        Path northNeedle = new Path();
        northNeedle.moveTo(cx, cy - needleR);
        northNeedle.lineTo(cx - needleHalfW, cy);
        northNeedle.lineTo(cx + needleHalfW, cy);
        northNeedle.close();
        needlePaint.setColor(Color.parseColor("#FF5252"));
        canvas.drawPath(northNeedle, needlePaint);

        Path southNeedle = new Path();
        southNeedle.moveTo(cx, cy + needleR);
        southNeedle.lineTo(cx - needleHalfW, cy);
        southNeedle.lineTo(cx + needleHalfW, cy);
        southNeedle.close();
        needlePaint.setColor(Color.parseColor("#E8EDF7"));
        canvas.drawPath(southNeedle, needlePaint);

        canvas.restore();

        canvas.drawCircle(cx, cy, 9f, centerPaint);
        canvas.drawCircle(cx, cy, 4f, innerDotPaint);
    }
}
