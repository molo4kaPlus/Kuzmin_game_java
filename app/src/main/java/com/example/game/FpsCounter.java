package com.example.game;

import android.graphics.Canvas;
import android.graphics.Paint;

public class FpsCounter {
    private Paint paint;
    private int frameCount = 0;
    private long lastFpsTime = 0;
    private int fps = 0;

    public FpsCounter() {
        paint = new Paint();
        paint.setColor(0xFF00FF00);
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setAntiAlias(true);
    }

    public void update() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsTime >= 1000) {
            fps = frameCount;
            frameCount = 0;
            lastFpsTime = currentTime;
        }
    }

    public void draw(Canvas canvas) {
        if (canvas != null) {
            int screenWidth = canvas.getWidth();
            canvas.drawText("FPS: " + fps, screenWidth - 20, 50, paint);
        }
    }

    public int getFps() {
        return fps;
    }
}