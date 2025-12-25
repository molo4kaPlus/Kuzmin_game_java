package com.example.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class SpriteManager {
    private static SpriteManager instance;
    private Context context;
    private Map<String, Bitmap> sprites;

    // Константы для имен спрайтов
    public static final String SPRITE_BATTERY = "battery";
    public static final String SPRITE_WIRE = "wire";
    public static final String SPRITE_BUTTON = "button";
    public static final String SPRITE_PSHALF = "ps_half";
    public static final String SPRITE_RJ45 = "RJ45";
    // Добавьте другие спрайты по мере необходимости

    private SpriteManager(Context context) {
        this.context = context.getApplicationContext();
        this.sprites = new HashMap<>();
        loadAllSprites();
    }

    public static synchronized SpriteManager getInstance(Context context) {
        if (instance == null) {
            instance = new SpriteManager(context);
        }
        return instance;
    }

    public static synchronized SpriteManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SpriteManager must be initialized with context first");
        }
        return instance;
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SpriteManager(context);
        }
    }

    private void loadAllSprites() {
        try {
            loadSprite(SPRITE_BATTERY, "sprites/battery.png");
            loadSprite(SPRITE_WIRE, "sprites/wire.png");
            loadSprite(SPRITE_BUTTON, "sprites/button.png");
            loadSprite(SPRITE_PSHALF, "sprites/psHalf.png");
            loadSprite(SPRITE_RJ45, "sprites/RJ45.png");

            Log.d("myLog", "All sprites loaded successfully");
        } catch (Exception e) {
            Log.e("myLog", "Error loading sprites: " + e.getMessage());
        }
    }

    private void loadSprite(String spriteName, String assetPath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(
                    context.getAssets().open(assetPath)
            );
            sprites.put(spriteName, bitmap);
            Log.d("myLog", "Sprite loaded: " + spriteName + " from " + assetPath);
        } catch (Exception e) {
            Log.e("myLog", "Failed to load sprite: " + spriteName + " from " + assetPath, e);
            sprites.put(spriteName, null); // Помечаем как неудачную загрузку
        }
    }

    public Bitmap getSprite(String spriteName) {
        return sprites.get(spriteName);
    }

    public boolean hasSprite(String spriteName) {
        return sprites.containsKey(spriteName) && sprites.get(spriteName) != null;
    }

    public void loadCustomSprite(String spriteName, String assetPath) {
        loadSprite(spriteName, assetPath);
    }

    public void release() {
        for (Bitmap bitmap : sprites.values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        sprites.clear();
        instance = null;
        Log.d("myLog", "SpriteManager released");
    }

    public int getLoadedCount() {
        int count = 0;
        for (Bitmap bitmap : sprites.values()) {
            if (bitmap != null) count++;
        }
        return count;
    }
}