package com.example.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private Paint paint;
    FpsCounter fpsCounter = new FpsCounter();
    private Level level;
    private boolean isPaused = false;
    private GameManager gameManager;

    private boolean gameOver = false;
    private boolean gameWon = false;
    private Paint gameOverPaint;
    private Paint gameOverTextPaint;
    private Paint gameOverSubTextPaint;
    private RectF restartButtonRect;
    private Paint restartButtonPaint;
    private Paint restartButtonTextPaint;
    private Paint restartButtonPressedPaint;
    private Paint gameWonPaint;
    private Paint gameWonTextPaint;
    private Paint gameWonSubTextPaint;
    private boolean restartButtonPressed = false;

    public GameView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(50);

        level = new Level(2, 1);
        level.addModule("Timer",0,0,1L);
        level.addModule("WordDisplay", 0, 1, 1);

        GameManager.init(context);
        gameManager = GameManager.getInstance();
        gameManager.setCurrentLevel(level);
        gameManager.startGame(level);

        gameThread = new GameThread(getHolder(), this);
        setFocusable(true);
        initPaints();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (gameOver || gameWon) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (restartButtonRect.contains(x, y)) {
                        restartButtonPressed = true;
                        invalidate();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (restartButtonPressed && restartButtonRect.contains(x, y)) {
                        restartButtonPressed = false;
                        restartGame();
                        invalidate();
                    } else {
                        restartButtonPressed = false;
                        invalidate();
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    boolean nowInside = restartButtonRect.contains(x, y);
                    if (restartButtonPressed != nowInside) {
                        restartButtonPressed = nowInside;
                        invalidate();
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    restartButtonPressed = false;
                    invalidate();
                    break;
            }
            return true;
        }

        boolean handled = gameManager.handleTouch(x, y);
        if (handled) {
            invalidate();
        }
        return true;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (gameThread == null) {
            gameThread = new GameThread(getHolder(), this);
            gameThread.setRunning(true);
            gameThread.start();
        } else {
            gameThread.setRunning(true);
        }
        isPaused = false;
        resumeAllTimers();
        gameManager.resumeGame();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pauseAllTimers();
        gameManager.pauseGame();
        if (gameThread != null) {
            gameThread.setRunning(false);
            boolean retry = true;
            int attempts = 0;
            while (retry && attempts < 10) {
                try {
                    gameThread.join(100);
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                attempts++;
            }
            gameThread = null;
        }
        isPaused = true;
    }
    public void update() {
        fpsCounter.update();

    }
    @Override
    public void draw(Canvas canvas) {
        if (gameOver) {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), gameOverPaint);

            float centerX = canvas.getWidth() / 2f;
            float centerY = canvas.getHeight() / 2f - 100;
            canvas.drawText("BOOM!", centerX, centerY, gameOverTextPaint);
            canvas.drawText("Игра окончена", centerX, centerY + 120, gameOverSubTextPaint);
            canvas.drawText("3 ошибки", centerX, centerY + 200, gameOverSubTextPaint);

            drawRestartButton(canvas, centerX, centerY + 350);
        } else if (gameWon) {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), gameWonPaint);

            float centerX = canvas.getWidth() / 2f;
            float centerY = canvas.getHeight() / 2f - 100;
            canvas.drawText("Победа!", centerX, centerY, gameWonTextPaint);
            canvas.drawText("Все модули обезврежены", centerX, centerY + 120, gameWonSubTextPaint);
            canvas.drawText("Оставшееся время: " + String.valueOf(level.getTimeLeft()), centerX, centerY + 240, gameOverSubTextPaint);

            drawRestartButton(canvas, centerX, centerY + 350);
        }
        else{
            super.draw(canvas);
            if (canvas != null) {
                if (level.getCellSize() == 0) {
                    level.setScreenDimensions(canvas.getWidth(), canvas.getHeight());
                    level.requestBombRedraw();
                }
                level.draw(canvas);
                fpsCounter.draw(canvas);
            }
        }
    }
    private void drawRestartButton(Canvas canvas, float centerX, float buttonY) {
        float buttonWidth = canvas.getWidth() * 0.6f;
        float buttonHeight = 140;

        restartButtonRect.set(
                centerX - buttonWidth / 2,
                buttonY - buttonHeight / 2,
                centerX + buttonWidth / 2,
                buttonY + buttonHeight / 2
        );

        Paint buttonPaint = restartButtonPressed ? restartButtonPressedPaint : restartButtonPaint;
        canvas.drawRoundRect(restartButtonRect, 30, 30, buttonPaint);

        float textY = buttonY - (restartButtonTextPaint.descent() + restartButtonTextPaint.ascent()) / 2;
        canvas.drawText("Начать заново", centerX, textY, restartButtonTextPaint);
    }
    class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private GameView gameView;
        private boolean running;

        public GameThread(SurfaceHolder holder, GameView view) {
            surfaceHolder = holder;
            gameView = view;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        if (canvas != null) {
                            gameView.update();
                            gameView.draw(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
    public void freeze() {
        if (gameThread != null) {
            gameThread.setRunning(false);
        }
        pauseAllTimers();
        gameManager.pauseGame();
        isPaused = true;
    }
    public void unfreeze() {
        isPaused = false;
        resumeAllTimers();
        gameManager.resumeGame();
        if (gameThread != null) {
            gameThread.setRunning(true);
            if (!gameThread.isAlive()) {
                gameThread.start();
            }
        }
    }
    private void pauseAllTimers() {
        for (Module module : level.getModules()) {
            if (module instanceof ModuleTimer) {
                ((ModuleTimer) module).pauseTimerForBackground();
            }
        }
        SoundManager.getInstance().stopAllSounds();
    }
    private void resumeAllTimers() {
        for (Module module : level.getModules()) {
            if (module instanceof ModuleTimer) {
                ((ModuleTimer) module).resumeTimerFromBackground();
                ((ModuleTimer) module).enableSound(true);
            }
        }
    }
    public void setGameOver(boolean over) {
        this.gameOver = over;
    }
    private void initPaints(){
        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.argb(180, 255, 0, 0)); // Полупрозрачный красный оверлей
        gameOverPaint.setStyle(Paint.Style.FILL);

        gameOverTextPaint = new Paint();
        gameOverTextPaint.setColor(Color.WHITE);
        gameOverTextPaint.setTextSize(150);
        gameOverTextPaint.setTextAlign(Paint.Align.CENTER);
        gameOverTextPaint.setAntiAlias(true);
        gameOverTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        gameOverSubTextPaint = new Paint();
        gameOverSubTextPaint.setColor(Color.WHITE);
        gameOverSubTextPaint.setTextSize(60);
        gameOverSubTextPaint.setTextAlign(Paint.Align.CENTER);
        gameOverSubTextPaint.setAntiAlias(true);

        // Кнопка "Начать заново"
        restartButtonPaint = new Paint();
        restartButtonPaint.setColor(Color.rgb(0.2f, 0.2f, 0.2f)); // Тёмно-зелёный
        restartButtonPaint.setStyle(Paint.Style.FILL);
        restartButtonPaint.setAntiAlias(true);

        restartButtonPressedPaint = new Paint();
        restartButtonPressedPaint.setColor(Color.rgb(200, 200, 200)); // Ярче при нажатии
        restartButtonPressedPaint.setStyle(Paint.Style.FILL);
        restartButtonPressedPaint.setAntiAlias(true);

        restartButtonTextPaint = new Paint();
        restartButtonTextPaint.setColor(Color.WHITE);
        restartButtonTextPaint.setTextSize(70);
        restartButtonTextPaint.setTextAlign(Paint.Align.CENTER);
        restartButtonTextPaint.setAntiAlias(true);
        restartButtonTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        restartButtonRect = new RectF();

        // Экран победы
        gameWonPaint = new Paint();
        gameWonPaint.setColor(Color.argb(180, 0, 100, 0)); // Полупрозрачный зелёный
        gameWonPaint.setStyle(Paint.Style.FILL);

        gameWonTextPaint = new Paint();
        gameWonTextPaint.setColor(Color.WHITE);
        gameWonTextPaint.setTextSize(150);
        gameWonTextPaint.setTextAlign(Paint.Align.CENTER);
        gameWonTextPaint.setAntiAlias(true);
        gameWonTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        gameWonSubTextPaint = new Paint();
        gameWonSubTextPaint.setColor(Color.WHITE);
        gameWonSubTextPaint.setTextSize(60);
        gameWonSubTextPaint.setTextAlign(Paint.Align.CENTER);
        gameWonSubTextPaint.setAntiAlias(true);
    }
    private void restartGame() {
        gameOver = false;
        this.gameWon = false;
        restartButtonPressed = false;
        gameManager.resetGame();
    }
    public void setGameWon(boolean won) {
        this.gameWon = won;
        this.gameOver = false;
        if (won) {
            pauseAllTimers();
        }
    }
}