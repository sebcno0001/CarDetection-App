package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawBoxes extends View {

    private List<DetectionResult> detections = new ArrayList<>();
    private int videoOriginalW = 0, videoOriginalH = 0;

    private int displayW = 0, displayH = 0;
    private int offsetX = 0, offsetY = 0;

    private final Paint boxPaint = new Paint();
    private final Paint textPaint = new Paint();

    public DrawBoxes(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        boxPaint.setColor(Color.RED);
        boxPaint.setStrokeWidth(5f);
        boxPaint.setStyle(Paint.Style.STROKE);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setVideoOriginalSize(int w, int h) {
        this.videoOriginalW = w;
        this.videoOriginalH = h;
    }

    public void setVideoDisplayInfo(int w, int h, int offX, int offY) {
        this.displayW = w;
        this.displayH = h;
        this.offsetX = offX;
        this.offsetY = offY;
        invalidate();
    }

    public void setDetections(List<DetectionResult> det) {
        this.detections = det;
        invalidate();
    }

    public void clearDetections() {
        this.detections.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (videoOriginalW == 0 || videoOriginalH == 0) return;

        float scaleX = (float) displayW / videoOriginalW;
        float scaleY = (float) displayH / videoOriginalH;

        for (DetectionResult d : detections) {
            RectF box = d.getBoundingBox();

            float left = offsetX + box.left * scaleX;
            float top = offsetY + box.top * scaleY;
            float right = offsetX + box.right * scaleX;
            float bottom = offsetY + box.bottom * scaleY;

            canvas.drawRect(left, top, right, bottom, boxPaint);
            canvas.drawText(d.getLabelWithConfidence(), left, top - 10, textPaint);
        }
    }
}
