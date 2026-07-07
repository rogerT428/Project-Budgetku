package com.mobile.uph24si3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    public static class PieSlice {
        public String label;
        public float value;
        public int color;

        public PieSlice(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private List<PieSlice> slices = new ArrayList<>();
    private Paint slicePaint;
    private Paint textPaint;
    private Paint centerPaint;
    private RectF oval = new RectF();
    private String centerText = "";
    private String centerSubText = "";

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slicePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(30f);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.parseColor("#161B22"));
        centerPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<PieSlice> slices, String centerText, String centerSubText) {
        this.slices = slices;
        this.centerText = centerText;
        this.centerSubText = centerSubText;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (slices == null || slices.isEmpty()) {
            drawEmpty(canvas);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        float padding = size * 0.08f;

        float cx = width / 2f;
        float cy = height / 2f;
        float radius = (size / 2f) - padding;
        float innerRadius = radius * 0.55f; // donut hole

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Calculate total value
        float total = 0;
        for (PieSlice s : slices) total += s.value;

        // Draw slices
        float startAngle = -90f;
        for (PieSlice slice : slices) {
            float sweepAngle = (slice.value / total) * 360f;
            slicePaint.setColor(slice.color);
            canvas.drawArc(oval, startAngle, sweepAngle, true, slicePaint);
            startAngle += sweepAngle;
        }

        // Draw donut hole (center circle)
        centerPaint.setColor(Color.parseColor("#0D1117"));
        canvas.drawCircle(cx, cy, innerRadius, centerPaint);

        // Draw center text
        textPaint.setColor(Color.parseColor("#F0F6FC"));
        textPaint.setTextSize(size * 0.07f);
        textPaint.setFakeBoldText(true);
        canvas.drawText(centerText, cx, cy - size * 0.02f, textPaint);

        textPaint.setTextSize(size * 0.045f);
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#8B949E"));
        canvas.drawText(centerSubText, cx, cy + size * 0.07f, textPaint);
    }

    private void drawEmpty(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float cx = width / 2f;
        float cy = height / 2f;
        int size = Math.min(width, height);
        float radius = (size / 2f) - size * 0.08f;

        Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setColor(Color.parseColor("#21262D"));
        emptyPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius, emptyPaint);

        emptyPaint.setColor(Color.parseColor("#0D1117"));
        canvas.drawCircle(cx, cy, radius * 0.55f, emptyPaint);

        textPaint.setColor(Color.parseColor("#8B949E"));
        textPaint.setTextSize(size * 0.06f);
        canvas.drawText("Tidak ada data", cx, cy + size * 0.02f, textPaint);
    }
}
