package com.baidumap.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {

    private Paint circlePaint;
    private Paint tickPaint;
    private Paint dirPaint;
    private Paint needlePaint;
    private Paint centerPaint;
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
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#2C3E50"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(3f);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#555555"));
        tickPaint.setStrokeWidth(2f);

        dirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dirPaint.setColor(Color.WHITE);
        dirPaint.setTextAlign(Paint.Align.CENTER);
        dirPaint.setTextSize(36f);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.parseColor("#FFD700"));
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

        canvas.drawCircle(cx, cy, radius, circlePaint);
        canvas.drawCircle(cx, cy, radius - 10f, circlePaint);

        for (int i = 0; i < 360; i += 5) {
            float rad = (float) Math.toRadians(i - 90);
            float innerR = (i % 30 == 0) ? radius - 30f : radius - 18f;
            float outerR = radius - 8f;
            canvas.drawLine(
                cx + innerR * (float) Math.cos(rad),
                cy + innerR * (float) Math.sin(rad),
                cx + outerR * (float) Math.cos(rad),
                cy + outerR * (float) Math.sin(rad),
                tickPaint
            );
        }

        canvas.save();
        canvas.rotate(-degree, cx, cy);

        String[] dirs = {"N", "E", "S", "W"};
        for (int i = 0; i < 4; i++) {
            float rad = (float) Math.toRadians(i * 90 - 90);
            float tx = cx + (radius - 50f) * (float) Math.cos(rad);
            float ty = cy + (radius - 50f) * (float) Math.sin(rad) + 12f;
            dirPaint.setColor(dirs[i].equals("N") ? Color.RED : Color.WHITE);
            canvas.drawText(dirs[i], tx, ty, dirPaint);
        }

        Path northNeedle = new Path();
        northNeedle.moveTo(cx, cy - radius + 40f);
        northNeedle.lineTo(cx - 12f, cy);
        northNeedle.lineTo(cx + 12f, cy);
        northNeedle.close();
        needlePaint.setColor(Color.parseColor("#E74C3C"));
        canvas.drawPath(northNeedle, needlePaint);

        Path southNeedle = new Path();
        southNeedle.moveTo(cx, cy + radius - 40f);
        southNeedle.lineTo(cx - 12f, cy);
        southNeedle.lineTo(cx + 12f, cy);
        southNeedle.close();
        needlePaint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawPath(southNeedle, needlePaint);

        canvas.restore();

        canvas.drawCircle(cx, cy, 8f, centerPaint);
        canvas.drawCircle(cx, cy, 4f, new Paint(Paint.ANTI_ALIAS_FLAG) {{ setColor(Color.parseColor("#1A1A2E")); }});
    }
}
