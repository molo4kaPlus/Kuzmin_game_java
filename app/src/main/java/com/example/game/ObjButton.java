package com.example.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

public class ObjButton extends Obj {
    private String label;
    private boolean isPressed = false;

    private long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_MS = 500;

    private Paint buttonPaint;
    private Paint pressedPaint;
    private Paint textPaint;

    private static final int[] COLORS = {
            Color.RED,       // Красный
            Color.GREEN,     // Зелёный
            Color.BLUE,      // Синий
            Color.YELLOW    // Жёлтый
    };

    private static final Random random = new Random();

    public ObjButton(int row, int col) {
        super(row, col);

        this.label = String.valueOf(random.nextInt(11)); // 0..10

        initializePaints();
        setSolved(false);
    }

    private void initializePaints() {
        int randomColor = COLORS[random.nextInt(COLORS.length)];

        buttonPaint = new Paint();
        buttonPaint.setColor(randomColor);
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setAntiAlias(true);

        pressedPaint = new Paint();
        pressedPaint.setColor(Color.rgb(150, 150, 150));
        pressedPaint.setStyle(Paint.Style.FILL);
        pressedPaint.setAntiAlias(true);

        textPaint = new Paint();

        int textColor;
        if (randomColor == Color.BLUE || randomColor == Color.rgb(128, 0, 128)) {
            textColor = Color.WHITE;
        } else {
            textColor = Color.BLACK;
        }

        textPaint.setColor(textColor);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds) {
        if (canvas == null || bounds == null) return;

        canvas.save();
        canvas.rotate(rotation, bounds.centerX(), bounds.centerY());
        Paint currentPaint = isPressed ? pressedPaint : buttonPaint;

        float cornerRadius = bounds.width() * 0.15f;
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, currentPaint);

        float textY = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2;
        canvas.drawText(label, bounds.centerX(), textY, textPaint);

        canvas.restore();
    }

    @Override
    public void onClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= CLICK_DEBOUNCE_MS) {
            isPressed = !isPressed;
            lastClickTime = currentTime;
            Log.d("myLog", "Button '" + label + "' pressed: " + isPressed + ", solved: " + isSolved());
        }
    }

    @Override
    public String getType() {
        return "BUTTON";
    }

    public boolean isPressed() {
        return isPressed;
    }

    public int getColor(){
        return buttonPaint.getColor();
    }
}