package com.example.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Module {
    private String name;
    private boolean isActive;
    private int row;
    private int col;

    private Paint modulePaint;
    private Paint textPaint;
    private Paint activePaint;

    public Module(String name, int row, int col) {
        this.name = name;
        this.row = row;
        this.col = col;
        this.isActive = false;

        initializePaints();
    }

    private void initializePaints() {
        modulePaint = new Paint();
        modulePaint.setColor(Color.rgb(50, 50, 50));
        modulePaint.setStyle(Paint.Style.FILL);
        modulePaint.setAntiAlias(true);

        activePaint = new Paint();
        activePaint.setColor(Color.rgb(100, 150, 255));
        activePaint.setStyle(Paint.Style.FILL);
        activePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    public void draw(Canvas canvas, RectF bounds) {
        Paint currentPaint = isActive ? activePaint : modulePaint;
        canvas.drawRect(bounds, currentPaint);

        if (name != null && !name.isEmpty()) {
            float textY = bounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(name, bounds.centerX(), textY, textPaint);
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
}
