package com.example.game;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class FontManager {
    private static final Map<String, Typeface> fonts = new HashMap<>();
    private static boolean isInitialized = false;
    private static Context appContext;

    // Константы для имен шрифтов
    public static final String FONT_SEVEN_SEGMENT = "SevenSegment";
    public static final String FONT_ANOTHER_FONT = "AnotherFont"; // добавьте свое имя

    public static void initialize(Context context) {
        if (isInitialized) {
            return;
        }
        appContext = context.getApplicationContext();
        try {
            // Загружаем два шрифта по их путям и присваиваем им имена
            loadFont(FONT_SEVEN_SEGMENT, "fonts/seven_segment.ttf");
            loadFont(FONT_ANOTHER_FONT, "fonts/another_font.ttf"); // второй шрифт
            isInitialized = true;
            Log.d("myLog", "Шрифты успешно загружены");
        } catch (Exception e) {
            Log.e("myLog", "Ошибка загрузки шрифтов: " + e.getMessage());
        }
    }

    private static void loadFont(String fontName, String fontPath) {
        try {
            Typeface typeface = Typeface.createFromAsset(appContext.getAssets(), fontPath);
            fonts.put(fontName, typeface); // Ключ - имя шрифта, значение - Typeface
            Log.d("myLog", "Загружен шрифт: " + fontName + " из " + fontPath);
        } catch (Exception e) {
            Log.e("myLog", "Не удалось загрузить шрифт: " + fontName + " (" + fontPath + ")");
        }
    }

    public static Typeface getFont(String fontKey) {
        if (!isInitialized) {
            Log.w("myLog", "FontManager не инициализирован!");
            return getDefaultFont();
        }

        Typeface font = fonts.get(fontKey);
        if (font == null) {
            Log.w("myLog", "Шрифт не найден: " + fontKey + ". Используется стандартный.");
            return getDefaultFont();
        }
        return font;
    }
    private static Typeface getDefaultFont() {
        return Typeface.DEFAULT;
    }
    public static String[] getLoadedFonts() {
        return fonts.keySet().toArray(new String[0]);
    }
    public static void clear() {
        fonts.clear();
        isInitialized = false;
        appContext = null;
    }
}