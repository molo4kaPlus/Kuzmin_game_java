package com.example.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class ObjRJ45 extends Obj {
    private Bitmap sprite;
    private Paint outlinePaint;

    // Дополнительные параметры, если нужны
    private String type = "RJ45";
    private boolean isActive = false;

    public ObjRJ45() {
        super();
        setSolved(true);
        initializePaints();
        loadSprite();
    }

    public ObjRJ45(int row, int col) {
        super(row, col);
        initializePaints();
        loadSprite();
    }

    public ObjRJ45(int row, int col, String type) {
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
                // Пытаемся получить спрайт RJ45
                sprite = spriteManager.getSprite("RJ45");
                if (sprite == null) {
                    // Если спрайт не найден, пробуем другие варианты имен
                    sprite = spriteManager.getSprite("rj_45");
                    if (sprite == null) {
                        sprite = spriteManager.getSprite("ethernet");
                        if (sprite == null) {
                            // Используем запасной спрайт (например, батарейку)
                            sprite = spriteManager.getSprite(SpriteManager.SPRITE_BATTERY);
                            Log.d("myLog", "RJ45 sprite not found, using fallback");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("myLog", "Error loading RJ45 sprite from SpriteManager: " + e.getMessage());
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
    }

    @Override
    public void draw(Canvas canvas, RectF bounds) {
        if (canvas == null || bounds == null) return;

        canvas.save();

        if (sprite != null) {
            // Рисуем спрайт
            drawSprite(canvas, bounds);
        } else {
            // Если спрайт не загрузился, рисуем простую иконку
            drawSimpleRJ45(canvas, bounds);
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

    private void drawSimpleRJ45(Canvas canvas, RectF bounds) {
        // Простой рисунок разъема RJ45 (Ethernet) как запасной вариант
        float padding = Math.min(bounds.width(), bounds.height()) * 0.15f;
        float width = bounds.width() - 2 * padding;
        float height = bounds.height() - 2 * padding;

        // Основной прямоугольник (корпус разъема)
        RectF mainRect = new RectF(
                bounds.centerX() - width / 2,
                bounds.centerY() - height / 2,
                bounds.centerX() + width / 2,
                bounds.centerY() + height / 2
        );

        // Цвет корпуса
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.rgb(80, 80, 80)); // Темно-серый
        bgPaint.setStyle(Paint.Style.FILL);

        float cornerRadius = width * 0.1f;
        canvas.drawRoundRect(mainRect, cornerRadius, cornerRadius, bgPaint);

        // Защелка (пластиковый язычок)
        float latchWidth = width * 0.3f;
        float latchHeight = height * 0.15f;
        RectF latchRect = new RectF(
                bounds.centerX() - latchWidth / 2,
                mainRect.top - latchHeight * 0.3f,
                bounds.centerX() + latchWidth / 2,
                mainRect.top + latchHeight * 0.7f
        );

        Paint latchPaint = new Paint();
        latchPaint.setColor(Color.rgb(120, 120, 120));
        latchPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(latchRect, latchPaint);

        // Контакты (8 маленьких прямоугольников)
        int pinCount = 8;
        float pinWidth = width * 0.08f;
        float pinHeight = height * 0.4f;
        float pinSpacing = (width - pinWidth * pinCount) / (pinCount + 1);

        Paint pinPaint = new Paint();
        pinPaint.setColor(Color.rgb(180, 180, 180)); // Металлический цвет
        pinPaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < pinCount; i++) {
            float pinLeft = mainRect.left + pinSpacing + i * (pinWidth + pinSpacing);
            float pinTop = mainRect.centerY() - pinHeight / 2;

            canvas.drawRect(
                    pinLeft, pinTop,
                    pinLeft + pinWidth, pinTop + pinHeight,
                    pinPaint
            );
        }

        // Текст "RJ45"
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(height * 0.25f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        float textX = mainRect.centerX();
        float textY = mainRect.bottom - textPaint.getTextSize() * 0.3f;
        canvas.drawText("RJ45", textX, textY, textPaint);

        // Контур
        canvas.drawRoundRect(mainRect, cornerRadius, cornerRadius, outlinePaint);
    }

    @Override
    public void onClick() {
        Log.d("myLog", "RJ45 Port clicked! Type: " + type);
        // Можете добавить логику при клике, если нужно
        isActive = !isActive;
    }

    @Override
    public String getType() {
        return "RJ45";
    }

    // Геттеры и сеттеры

    public String getRJ45Type() {
        return type;
    }

    public void setRJ45Type(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Bitmap getSprite() {
        return sprite;
    }
}