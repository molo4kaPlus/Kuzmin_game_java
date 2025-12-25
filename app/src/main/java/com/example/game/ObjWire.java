package com.example.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

public class ObjWire extends Obj {
    private Paint wirePaint;
    private Paint contactPaint;
    private Paint highlightPaint;
    private Paint cutPaint;
    private Paint paddingPaint;
    private boolean isCut = false;
    private int wireColor;
    private int orientation;
    public boolean checked = false;

    private static final int[] WIRE_COLORS = {
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.BLUE,
            Color.GREEN
    };

    public ObjWire() {
        super();
        Random rand = new Random();
        rotation = rand.nextInt(4) * 90f;
        orientation = rand.nextInt(4);
        initializePaints();
    }

    private void initializePaints() {
        wireColor = WIRE_COLORS[(int)(Math.random() * WIRE_COLORS.length)];
        wirePaint = new Paint();
        wirePaint.setColor(wireColor);
        wirePaint.setStyle(Paint.Style.STROKE);
        wirePaint.setStrokeWidth(8);
        wirePaint.setAntiAlias(true);
        wirePaint.setStrokeCap(Paint.Cap.ROUND);

        contactPaint = new Paint();
        contactPaint.setColor(Color.rgb(200, 200, 200));
        contactPaint.setStyle(Paint.Style.FILL);
        contactPaint.setAntiAlias(true);

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.rgb(255, 255, 100));
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3);
        highlightPaint.setAntiAlias(true);

        cutPaint = new Paint();
        cutPaint.setColor(Color.RED);
        cutPaint.setStyle(Paint.Style.STROKE);
        cutPaint.setStrokeWidth(4);
        cutPaint.setAntiAlias(true);

        paddingPaint = new Paint();
        paddingPaint.setColor(Color.rgb(70, 70, 70));
        paddingPaint.setStyle(Paint.Style.FILL);
        paddingPaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds) {
        if (canvas == null || bounds == null) return;

        canvas.save();
        canvas.rotate(rotation, bounds.centerX(), bounds.centerY());

        float padding = 20f;
        float contactRadius = Math.min(bounds.width(), bounds.height()) / 10;

        // Фон ячейки
        canvas.drawRoundRect(bounds, 10f, 10f, paddingPaint);

        // Определяем точки начала и конца провода
        float startX, startY, endX, endY;

        switch (orientation) {
            case 0: // Диагональ \
                startX = bounds.left + padding;
                startY = bounds.top + padding;
                endX = bounds.right - padding;
                endY = bounds.bottom - padding;
                break;

            case 1: // Диагональ /
                startX = bounds.left + padding;
                startY = bounds.bottom - padding;
                endX = bounds.right - padding;
                endY = bounds.top + padding;
                break;

            case 2: // Горизонтально
                startX = bounds.left + padding;
                startY = bounds.centerY();
                endX = bounds.right - padding;
                endY = bounds.centerY();
                break;

            case 3: // Вертикально
                startX = bounds.centerX();
                startY = bounds.top + padding;
                endX = bounds.centerX();
                endY = bounds.bottom - padding;
                break;

            default:
                startX = bounds.left + padding;
                startY = bounds.top + padding;
                endX = bounds.right - padding;
                endY = bounds.bottom - padding;
                break;
        }

        // Рисуем контакты ТОЛЬКО на концах провода
        canvas.drawCircle(startX, startY, contactRadius, contactPaint);
        canvas.drawCircle(endX, endY, contactRadius, contactPaint);

        // Рисуем провод
        if (!isCut) {
            canvas.drawLine(startX, startY, endX, endY, wirePaint);
        } else {
            // Разрезанный провод
            float midX = (startX + endX) / 2f;
            float midY = (startY + endY) / 2f;

            float gap = 12f;
            float dx = endX - startX;
            float dy = endY - startY;
            float length = (float) Math.hypot(dx, dy);
            if (length == 0) length = 1f; // защита от деления на 0
            float unitX = dx / length;
            float unitY = dy / length;

            float cutStartX = midX - unitX * gap;
            float cutStartY = midY - unitY * gap;
            float cutEndX = midX + unitX * gap;
            float cutEndY = midY + unitY * gap;

            canvas.drawLine(startX, startY, cutStartX, cutStartY, wirePaint);
            canvas.drawLine(cutEndX, cutEndY, endX, endY, wirePaint);

            // Красная линия разреза
            float perpX = -unitY;
            float perpY = unitX;
            float cutMarkLength = 22f;

            canvas.drawLine(
                    midX - perpX * cutMarkLength / 2,
                    midY - perpY * cutMarkLength / 2,
                    midX + perpX * cutMarkLength / 2,
                    midY + perpY * cutMarkLength / 2,
                    cutPaint
            );
        }

        canvas.restore();
    }

    @Override
    public String getType() {
        return "WIRE";
    }

    @Override
    public void onClick() {
        if (isCut) return;
        isCut = true;
        Log.d("myLog", "Wire clicked! Orientation: " + orientation + ", Cut state: " + isCut);
    }

    public boolean isCut() {
        return isCut;
    }

    public int getColor() {
        return wireColor;
    }
}