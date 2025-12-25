package com.example.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class ObjBattery extends Obj {
    private Bitmap batterySprite;
    private Paint outlinePaint;

    private String batteryType = "AA";

    public ObjBattery() {
        super();
        setSolved(true);
        initializePaints();
        loadSprite();
    }

    public ObjBattery(int row, int col) {
        super(row, col);
        initializePaints();
        loadSprite();
    }

    public ObjBattery(int row, int col, String type) {
        super(row, col);
        this.batteryType = type;
        initializePaints();
        loadSprite();
    }

    private void loadSprite() {
        try {
            // Используем SpriteManager для загрузки спрайта
            SpriteManager spriteManager = SpriteManager.getInstance();
            if (spriteManager != null) {
                batterySprite = spriteManager.getSprite(SpriteManager.SPRITE_BATTERY);
                if (batterySprite == null) {
                    Log.d("myLog", "Battery sprite not found in SpriteManager");
                }
            }
        } catch (Exception e) {
            Log.d("myLog", "Error loading battery sprite from SpriteManager: " + e.getMessage());
            batterySprite = null;
        }
    }

    private void initializePaints() {
        // Контур для простого рисунка (если спрайт не загрузился)
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.rgb(100, 100, 100));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(2);
        outlinePaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds) {
        if (canvas == null || bounds == null) return;

        canvas.save();

        if (batterySprite != null) {
            // Рисуем спрайт
            drawSprite(canvas, bounds);
        } else {
            // Рисуем простую батарейку
            drawSimpleBattery(canvas, bounds);
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

        canvas.drawBitmap(batterySprite, null, spriteBounds, null);
    }

    private void drawSimpleBattery(Canvas canvas, RectF bounds) {
        // Простой рисунок батарейки, если спрайт не загрузился
        float padding = Math.min(bounds.width(), bounds.height()) * 0.15f;
        float width = bounds.width() - 2 * padding;
        float height = bounds.height() - 2 * padding;

        // Корпус батарейки
        RectF bodyRect = new RectF(
                bounds.centerX() - width / 2,
                bounds.centerY() - height / 2,
                bounds.centerX() + width / 2,
                bounds.centerY() + height / 2
        );

        // Цвет корпуса
        Paint bodyPaint = new Paint();
        bodyPaint.setColor(Color.rgb(220, 220, 220)); // Металлический серый
        bodyPaint.setStyle(Paint.Style.FILL);

        float cornerRadius = width * 0.1f;
        canvas.drawRoundRect(bodyRect, cornerRadius, cornerRadius, bodyPaint);

        // Верхний контакт (плюс)
        float contactWidth = width * 0.3f;
        float contactHeight = height * 0.15f;
        RectF contactRect = new RectF(
                bounds.centerX() - contactWidth / 2,
                bodyRect.top - contactHeight,
                bounds.centerX() + contactWidth / 2,
                bodyRect.top
        );

        Paint contactPaint = new Paint();
        contactPaint.setColor(Color.rgb(180, 180, 180));
        contactPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(contactRect, contactWidth * 0.1f, contactHeight * 0.1f, contactPaint);

        // Знак "+" на контакте
        Paint plusPaint = new Paint();
        plusPaint.setColor(Color.BLACK);
        plusPaint.setTextSize(contactHeight * 0.6f);
        plusPaint.setTextAlign(Paint.Align.CENTER);
        float plusX = contactRect.centerX();
        float plusY = contactRect.centerY() - (plusPaint.descent() + plusPaint.ascent()) / 2;
        canvas.drawText("+", plusX, plusY, plusPaint);

        // Знак "-" внизу
        Paint minusPaint = new Paint();
        minusPaint.setColor(Color.BLACK);
        minusPaint.setTextSize(height * 0.3f);
        minusPaint.setTextAlign(Paint.Align.CENTER);
        float minusX = bodyRect.centerX();
        float minusY = bodyRect.bottom - minusPaint.descent();
        canvas.drawText("-", minusX, minusY, minusPaint);

        // Контур
        canvas.drawRoundRect(bodyRect, cornerRadius, cornerRadius, outlinePaint);
    }

    @Override
    public void onClick() {
        Log.d("myLog", "Battery clicked! Type: " + batteryType);
        // Просто логируем клик, батарейка ничего не делает
    }

    @Override
    public String getType() {
        return "BATTERY";
    }

    // Геттеры и сеттеры

    public String getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }
}