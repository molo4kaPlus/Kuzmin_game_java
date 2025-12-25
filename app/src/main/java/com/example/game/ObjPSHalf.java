package com.example.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class ObjPSHalf extends Obj {
    private Bitmap sprite;
    private Paint outlinePaint;

    // Дополнительные параметры, если нужны
    private String type = "HALF";
    private boolean isActive = false;
    private Paint paddingPaint;

    public ObjPSHalf() {
        super();
        setSolved(true);
        initializePaints();
        loadSprite();
    }

    public ObjPSHalf(int row, int col) {
        super(row, col);
        initializePaints();
        loadSprite();
    }

    public ObjPSHalf(int row, int col, String type) {
        super(row, col);
        this.type = type;
        initializePaints();
        loadSprite();
    }

    private void loadSprite() {
        try {
            // Используем SpriteManager для загрузки спрайта
            SpriteManager spriteManager = SpriteManager.getInstance();
            if (spriteManager != null) {
                // Предполагаем, что спрайт называется "ps_half" или аналогично
                // Можно добавить константу в SpriteManager для этого спрайта
                sprite = spriteManager.getSprite("ps_half");
                if (sprite == null) {
                    // Попробуем получить по другому имени или использовать запасной вариант
                    sprite = spriteManager.getSprite(SpriteManager.SPRITE_BATTERY); // Временно используем батарейку
                    Log.d("myLog", "PSHalf sprite not found, using fallback");
                }
            }
        } catch (Exception e) {
            Log.d("myLog", "Error loading PSHalf sprite from SpriteManager: " + e.getMessage());
            sprite = null;
        }
    }

    private void initializePaints() {
        // Контур для простого рисунка (если спрайт не загрузился)
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.rgb(150, 150, 150));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        outlinePaint.setAntiAlias(true);

        paddingPaint = new Paint();
        paddingPaint.setColor(Color.rgb(70, 70, 70));
        paddingPaint.setStyle(Paint.Style.FILL);
        paddingPaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds) {
        if (canvas == null || bounds == null) return;

        canvas.save();
        canvas.drawRoundRect(bounds, 10f, 10f, paddingPaint);
        if (sprite != null) {
            // Рисуем спрайт
            drawSprite(canvas, bounds);
        } else {
            // Рисуем простой графический элемент
            drawSimplePSHalf(canvas, bounds);
        }

        canvas.restore();
    }

    private void drawSprite(Canvas canvas, RectF bounds) {
        // Растягиваем спрайт под размер ячейки с небольшим отступом
        RectF spriteBounds = new RectF(
                bounds.left + bounds.width() * 0.1f,
                bounds.top + bounds.height() * 0.1f,
                bounds.right - bounds.width() * 0.1f,
                bounds.bottom - bounds.height() * 0.1f
        );

        canvas.drawBitmap(sprite, null, spriteBounds, null);
    }

    private void drawSimplePSHalf(Canvas canvas, RectF bounds) {
        // Простой рисунок элемента PS/2 (половинка)
        float padding = Math.min(bounds.width(), bounds.height()) * 0.15f;
        float width = bounds.width() - 2 * padding;
        float height = bounds.height() - 2 * padding;
        canvas.drawRoundRect(bounds, 10f, 10f, paddingPaint);
        // Основной прямоугольник
        RectF mainRect = new RectF(
                bounds.centerX() - width / 2,
                bounds.centerY() - height / 2,
                bounds.centerX() + width / 2,
                bounds.centerY() + height / 2
        );

        // Цвет фона
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.rgb(200, 200, 200)); // Серый
        bgPaint.setStyle(Paint.Style.FILL);

        float cornerRadius = width * 0.1f;
        canvas.drawRoundRect(mainRect, cornerRadius, cornerRadius, bgPaint);

        // Коннектор (6 контактов)
        float connectorWidth = width * 0.6f;
        float connectorHeight = height * 0.2f;
        RectF connectorRect = new RectF(
                bounds.centerX() - connectorWidth / 2,
                mainRect.top - connectorHeight * 0.5f,
                bounds.centerX() + connectorWidth / 2,
                mainRect.top + connectorHeight * 0.5f
        );

        Paint connectorPaint = new Paint();
        connectorPaint.setColor(Color.rgb(100, 100, 100));
        connectorPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(connectorRect, connectorPaint);

        // Контакты (6 маленьких прямоугольников)
        int pinCount = 6;
        float pinWidth = connectorWidth * 0.12f;
        float pinHeight = connectorHeight * 0.6f;
        float pinSpacing = (connectorWidth - pinWidth * pinCount) / (pinCount + 1);

        Paint pinPaint = new Paint();
        pinPaint.setColor(Color.rgb(60, 60, 60));
        pinPaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < pinCount; i++) {
            float pinLeft = connectorRect.left + pinSpacing + i * (pinWidth + pinSpacing);
            float pinTop = connectorRect.centerY() - pinHeight / 2;

            canvas.drawRect(
                    pinLeft, pinTop,
                    pinLeft + pinWidth, pinTop + pinHeight,
                    pinPaint
            );
        }

        // Текст "PS/2"
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(height * 0.3f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        float textX = mainRect.centerX();
        float textY = mainRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2;
        canvas.drawText("PS/2", textX, textY, textPaint);

        // Контур
        canvas.drawRoundRect(mainRect, cornerRadius, cornerRadius, outlinePaint);
    }

    @Override
    public void onClick() {
        Log.d("myLog", "PSHalf clicked! Type: " + type);
        // Можете добавить логику при клике, если нужно
        isActive = !isActive;
    }

    @Override
    public String getType() {
        return "PSHALF";
    }

    // Геттеры и сеттеры

    public String getPSHalfType() {
        return type;
    }

    public void setPSHalfType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}