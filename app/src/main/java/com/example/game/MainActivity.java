package com.example.game;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;

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
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.freeze();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.unfreeze();
        }
    }
    private void initAssets(){
        FontManager.initialize(this);
        SoundManager.init(this);
    }
}