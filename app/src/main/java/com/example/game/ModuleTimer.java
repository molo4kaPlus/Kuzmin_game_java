package com.example.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.util.Log;

public class ModuleTimer extends Module {
    private SoundManager soundManager;
    private long lastDisplayedSeconds = -1;
    private long timeLeftInMillis;
    private long totalTimeMillis;
    private CountDownTimer countDownTimer;
    private boolean isRunning;
    private Paint timePaint;
    private Paint displayPaint;
    private Paint displayBorderPaint;
    private Paint serialPaint;
    private String[] timeChars = new String[5];
    private String serialNumber;
    private long pauseTime = 0;
    private boolean soundEnabled = true;
    private long lastBeepTime = 0;
    private static final long BEEP_INTERVAL = 1000;

    // Счетчик ошибок
    private int errorCount = 0;
    private static final int MAX_ERRORS = 3;
    private Paint errorLightOnPaint;
    private Paint errorLightOffPaint;
    private Paint errorLightBorderPaint;
    private Paint stickerPaint;
    private Paint stickerBorderPaint;
    private Paint stickerShadowPaint;
    private Paint serialTextPaint;

    public ModuleTimer(int row, int col, long minutes) {
        super(row, col);
        this.setName("Timer");
        this.totalTimeMillis = minutes * 60 * 1000 / 2;
        this.timeLeftInMillis = totalTimeMillis;
        this.isRunning = false;
        this.errorCount = 0;
        setCellOccupied(0,4,true);
        setCellOccupied(0,3,true);
        setCellOccupied(1,2,true);
        setCellOccupied(0,2,true);
        setCellOccupied(1,1,true);
        setCellOccupied(0,1,true);
        initializeTimerPaints();
        soundManager = SoundManager.getInstance();
        this.serialNumber = generateSerialNumber();
        this.startTimer();
        Log.d("myLog", "ModuleTimer: added");
        addRandomObjects();
    }
    private void initializeTimerPaints() {
        serialPaint = new Paint();
        serialPaint.setColor(Color.rgb(150, 150, 150));
        serialPaint.setTextSize(40);
        serialPaint.setTextAlign(Paint.Align.RIGHT);
        serialPaint.setAntiAlias(true);
        serialPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

        displayPaint = new Paint();
        displayPaint.setColor(Color.rgb(20, 20, 20));
        displayPaint.setStyle(Paint.Style.FILL);
        displayPaint.setAntiAlias(true);

        displayBorderPaint = new Paint();
        displayBorderPaint.setColor(Color.rgb(100, 100, 100));
        displayBorderPaint.setStyle(Paint.Style.STROKE);
        displayBorderPaint.setStrokeWidth(3);
        displayBorderPaint.setAntiAlias(true);

        timePaint = new Paint();
        timePaint.setColor(Color.rgb(0, 255, 0));
        timePaint.setTextSize(90);
        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setAntiAlias(true);
        timePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        timePaint.setTypeface(FontManager.getFont("SevenSegment"));

        errorLightOnPaint = new Paint();
        errorLightOnPaint.setColor(Color.rgb(255, 50, 50));
        errorLightOnPaint.setStyle(Paint.Style.FILL);
        errorLightOnPaint.setAntiAlias(true);

        errorLightOffPaint = new Paint();
        errorLightOffPaint.setColor(Color.rgb(50, 20, 20));
        errorLightOffPaint.setStyle(Paint.Style.FILL);
        errorLightOffPaint.setAntiAlias(true);

        errorLightBorderPaint = new Paint();
        errorLightBorderPaint.setColor(Color.rgb(150, 150, 150));
        errorLightBorderPaint.setStyle(Paint.Style.STROKE);
        errorLightBorderPaint.setStrokeWidth(2);
        errorLightBorderPaint.setAntiAlias(true);

        stickerPaint = new Paint();
        stickerPaint.setColor(Color.rgb(255, 255, 240));
        stickerPaint.setStyle(Paint.Style.FILL);
        stickerPaint.setAntiAlias(true);

        stickerBorderPaint = new Paint();
        stickerBorderPaint.setColor(Color.rgb(100, 100, 100));
        stickerBorderPaint.setStyle(Paint.Style.STROKE);
        stickerBorderPaint.setStrokeWidth(3);
        stickerBorderPaint.setAntiAlias(true);

        stickerShadowPaint = new Paint();
        stickerShadowPaint.setColor(Color.argb(80, 0, 0, 0));
        stickerShadowPaint.setStyle(Paint.Style.FILL);
        stickerShadowPaint.setAntiAlias(true);

        serialTextPaint = new Paint();
        serialTextPaint.setColor(Color.BLACK);
        serialTextPaint.setTextSize(42);
        serialTextPaint.setTextAlign(Paint.Align.CENTER);
        serialTextPaint.setAntiAlias(true);
        serialTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
    }
    public void startTimer() {
        if (isRunning || countDownTimer != null) {
            return;
        }
        updateTimeChars();
        setActive(true);
        isRunning = true;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;

                long secondsLeft = millisUntilFinished / 1000;
                if (secondsLeft != lastDisplayedSeconds) {
                    lastDisplayedSeconds = secondsLeft;
                    updateTimeChars();
                }
                if (soundEnabled && secondsLeft > 0 && secondsLeft <= 150) {
                    playBeepSound(secondsLeft);
                }
                if (soundEnabled && secondsLeft == 0)
                {
                    playBoomSound(secondsLeft);
                    }
//                else if (soundEnabled && ) {  // бипка пердежа под луз
//
//                    }

            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isRunning = false;
                setActive(false);
                triggerTimerFinished();
            }
        }.start();
    }
    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    public void resetTimer() {
        isRunning = true;
        setActive(true);
        clearObjects();
        timeLeftInMillis = 1 * 60 * 1000;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;

                long secondsLeft = millisUntilFinished / 1000;
                if (secondsLeft != lastDisplayedSeconds) {
                    lastDisplayedSeconds = secondsLeft;
                    updateTimeChars();
                }
                if (soundEnabled && secondsLeft > 0 && secondsLeft <= 60) {
                    playBeepSound(secondsLeft);
                }
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isRunning = false;
                setActive(false);
                triggerTimerFinished();
            }
        }.start();
        serialNumber = generateSerialNumber();
    }
    public void subtractTime(long seconds) {
        timeLeftInMillis -= seconds * 1000;
        if (timeLeftInMillis < 0) {
            timeLeftInMillis = 0;
        }
    }
    private void triggerTimerFinished() {
        Log.d("myLog", "triggerTimerFinished");
    }
    @Override
    public void draw(Canvas canvas, RectF bounds) {
        super.draw(canvas, bounds);

        float displayHeightRatio = 0.3f;
        float displayWidthRatio = 0.4f;
        float displayHeight = bounds.height() * displayHeightRatio;
        float displayWidth = bounds.width() * displayWidthRatio;
        float topOffset = bounds.height() * 0.04f;
        float displayPadding = 15f;

        RectF displayRect = new RectF(
                bounds.centerX() - displayWidth / 2,
                bounds.top + topOffset,
                bounds.centerX() + displayWidth / 2,
                bounds.top + topOffset + displayHeight
        );
        canvas.drawRect(displayRect, displayPaint);
        canvas.drawRect(displayRect, displayBorderPaint);

        float maxCharWidth = timePaint.measureText("8");
        float totalTextWidth = maxCharWidth * 5;
        float startX = displayRect.left + (displayRect.width() - totalTextWidth) / 2;
        float textY = displayRect.centerY() - ((timePaint.descent() + timePaint.ascent()) / 2);

        float currentX = startX;
        for (int i = 0; i < 5; i++) {
            float charX = currentX + (maxCharWidth / 2);
            canvas.drawText(timeChars[i], charX, textY, timePaint);
            currentX += maxCharWidth;
        }

        drawErrorLights(canvas, displayRect, bounds);

        // --- стикер с серийным номером ---
        canvas.save();
        canvas.translate(bounds.right, bounds.bottom);
        canvas.rotate(-12);

        float stickerWidth = bounds.width() * 0.25f;
        float stickerHeight = bounds.height() * 0.15f;

        float stickerX = -stickerWidth + bounds.width() * 0.18f;
        float stickerY = -stickerHeight - bounds.height() * 0.8f;

        RectF stickerRect = new RectF(stickerX, stickerY, stickerX + stickerWidth, stickerY + stickerHeight);

        canvas.drawRoundRect(
                new RectF(stickerX + 8, stickerY + 8, stickerX + stickerWidth + 8, stickerY + stickerHeight + 8),
                20, 20, stickerShadowPaint
        );
        canvas.drawRoundRect(stickerRect, 20, 20, stickerPaint);
        canvas.drawRoundRect(stickerRect, 20, 20, stickerBorderPaint);
        float textStickerY = stickerY + stickerHeight / 2 - (serialTextPaint.descent() + serialTextPaint.ascent()) / 2;
        canvas.drawText(serialNumber, stickerX + stickerWidth / 2, textStickerY, serialTextPaint);
        canvas.restore();
    }
    private void drawErrorLights(Canvas canvas, RectF displayRect, RectF bounds) {
        int lightCount = 3;
        float lightRadius = bounds.height() * 0.04f;
        float lightSpacing = lightRadius * 2f;
        float totalWidth = (lightCount * lightRadius * 2) + ((lightCount - 1) * lightSpacing);

        float lightsTop = displayRect.bottom + bounds.height() * 0.05f;
        float startX = bounds.centerX() - (totalWidth / 2) + lightRadius;

        for (int i = 0; i < lightCount; i++) {
            float centerX = startX + (i * (lightRadius * 2 + lightSpacing));
            float centerY = lightsTop + lightRadius;
            boolean isLightOn = (i < errorCount);
            Paint lightPaint = isLightOn ? errorLightOnPaint : errorLightOffPaint;
            canvas.drawCircle(centerX, centerY, lightRadius, lightPaint);
            canvas.drawCircle(centerX, centerY, lightRadius, errorLightBorderPaint);
            if (isLightOn) {
                Paint glowPaint = new Paint();
                glowPaint.setColor(Color.rgb(255, 100, 100));
                glowPaint.setStyle(Paint.Style.STROKE);
                glowPaint.setStrokeWidth(1);
                glowPaint.setAlpha(150);
                canvas.drawCircle(centerX, centerY, lightRadius + 2, glowPaint);
            }
        }
    }
    private String formatTime(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }
    public boolean isRunning() {
        return isRunning;
    }
    private void updateTimeChars() {
        String timeString = formatTime(timeLeftInMillis); // "MM:SS"
        for (int i = 0; i < 5 && i < timeString.length(); i++) {
            timeChars[i] = String.valueOf(timeString.charAt(i));
        }
    }
    public void pauseTimerForBackground() {
        if (isRunning && countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            pauseTime = System.currentTimeMillis();
        }
    }
    public void resumeTimerFromBackground() {
        if (pauseTime > 0) {
            long timePassedInBackground = System.currentTimeMillis() - pauseTime;
            timeLeftInMillis -= timePassedInBackground;

            pauseTime = 0;

            if (timeLeftInMillis <= 0) {
                timeLeftInMillis = 0;
                isRunning = false;
                setActive(false);
                triggerTimerFinished();
            } else {
                isRunning = false;
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                startTimer();
            }
        }
    }
    private String generateSerialNumber() {
        float temp = System.currentTimeMillis() % 10000;
        serial = (int) temp;
        return "TM-" + (int) temp;
    }
    private void playBeepSound(long secondsLeft) {
        if (soundManager != null && soundEnabled) {
            long currentTime = System.currentTimeMillis();

            if (secondsLeft > 0 && secondsLeft <= 60) {
                if (currentTime - lastBeepTime >= BEEP_INTERVAL) {
                    soundManager.playSound(SoundManager.SOUND_TIMER_BEEP1, 1.0f);
                    lastBeepTime = currentTime;
                    Log.d("myLog", "Beep at: " + secondsLeft + "s");
                }
            } else if (secondsLeft == 0) {
                soundManager.playSound(SoundManager.SOUND_TIMER_BEEP1, 1.0f);

            }
        }
    }

    private void playBoomSound(long secondsLeft) {
        if (soundManager != null && soundEnabled) {
            long currentTime = System.currentTimeMillis();

            if (secondsLeft == 0) {
                soundManager.playSound(SoundManager.SOUND_TIMER_BOOM1, 1.0f);
            }
        }
    }
    public void enableSound(boolean enable) {
        this.soundEnabled = enable;
    }
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    public void addError() {
        if (errorCount < MAX_ERRORS) {
            errorCount++;
            Log.d("myLog", "Timer error added. Total errors: " + errorCount);
        }
    }
    public void resetErrors() {
        errorCount = 0;
        Log.d("myLog", "Timer errors reset");
    }
    public int getErrorCount() {
        return errorCount;
    }
    public boolean isMaxErrorsReached() {
        return errorCount >= MAX_ERRORS;
    }
    public float getTimeLeft(){
        return timeLeftInMillis;
    }
    public int getSerial(){
        return serial;
    }
}