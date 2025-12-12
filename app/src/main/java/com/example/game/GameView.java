package com.example.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private Paint paint;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(50);

        gameThread = new GameThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);

            canvas.drawText("Game Running", 100, 100, paint);
            canvas.drawCircle(200, 300, 50, paint);
        }
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

                try {
                    sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}