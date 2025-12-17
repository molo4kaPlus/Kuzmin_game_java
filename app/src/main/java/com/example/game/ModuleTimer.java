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

    public ModuleTimer(int row, int col, long minutes) {
        super(row, col);
        this.setName("Timer");
        this.totalTimeMillis = minutes * 60 * 1000;
        this.timeLeftInMillis = totalTimeMillis;
        this.isRunning = false;
        initializeTimerPaints();
        soundManager = SoundManager.getInstance();
        this.serialNumber = generateSerialNumber();
        this.startTimer();
        Log.d("myLog", "ModuleTimer: added");
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
    }
    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    public void resetTimer() {
        pauseTimer();
        timeLeftInMillis = totalTimeMillis;
        isRunning = false;
        setActive(false);
        countDownTimer = null;
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
        float topOffset = bounds.height() * 0.1f;
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

        float serialX = bounds.right - 15;
        float serialY = bounds.top + 40;
        canvas.drawText(serialNumber, serialX, serialY, serialPaint);
    }
    private String formatTime(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        playBeepSound(getTimeLeftInMillis()/1000);
        return String.format("%02d:%02d", minutes, seconds);
    }
    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }
    public void setTimeLeftInMillis(long timeLeftInMillis) {
        this.timeLeftInMillis = timeLeftInMillis;
    }
    public long getTotalTimeMillis() {
        return totalTimeMillis;
    }
    public void setTotalTimeMillis(long totalTimeMillis) {
        this.totalTimeMillis = totalTimeMillis;
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
        return "TM-" + System.currentTimeMillis() % 10000;
    }
    private void playBeepSound(long secondsLeft) {
        if (soundManager != null) {
            if (secondsLeft <= 60) {
                soundManager.playSound(SoundManager.SOUND_TIMER_BEEP1, 1.0f);
            } else {

            }
        }
    }
    public void enableSound(boolean enable) {
        this.soundEnabled = enable;
    }
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}