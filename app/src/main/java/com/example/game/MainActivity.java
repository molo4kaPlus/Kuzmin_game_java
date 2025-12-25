package com.example.game;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity
        implements GameManager.GameEventListener {
    private GameView gameView;
    private GameManager gameManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        initAssets();

        gameView = new GameView(this);
        setContentView(gameView);

        gameManager = GameManager.getInstance();
        gameManager.setGameEventListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.freeze();
        }
        if (gameManager != null) {
            gameManager.pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.unfreeze();
        }
        if (gameManager != null) {
            gameManager.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameManager != null) {
            gameManager.cleanup();
        }
    }
    @Override
    public void onGameOver() {
        runOnUiThread(() -> {
            if (gameView != null) {
                gameView.setGameOver(true);
                //gameView.freeze();
            }
        });
    }

    @Override
    public void onScoreChanged(int newScore) {
        runOnUiThread(() -> {
            // Обновить отображение счета
        });
    }

    @Override
    public void onLivesChanged(int newLives) {
        runOnUiThread(() -> {
            // Обновить отображение жизней
        });
    }

    @Override
    public void onErrorAdded(int totalErrors) {
        runOnUiThread(() -> {
            // Обработать добавление ошибки
        });
    }

    @Override
    public void onTimerFinished(String moduleName) {
        runOnUiThread(() -> {
            // Обработать завершение таймера
        });
    }

    @Override
    public void onModuleActivated(String moduleName, boolean activated) {
        // Обработать активацию модуля
    }

    @Override
    public void onObjectClicked(String objectType, int row, int col) {
        // Обработать клик на объекте
    }
    @Override
    public void onGameWon() {
        runOnUiThread(() -> {
            if (gameView != null) {
                gameView.setGameWon(true);
            }
        });
    }
    private void initAssets(){
        FontManager.initialize(this);
        SoundManager.init(this);
        SpriteManager.init(this);
        GameManager.init(this); // Инициализируем GameManager
    }
}