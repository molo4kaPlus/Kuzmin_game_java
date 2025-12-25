package com.example.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

public class ModuleWords extends Module {
    private Paint displayPaint;
    private Paint displayBorderPaint;
    private Paint wordPaint;

    private Paint buttonPaint;
    private Paint buttonBorderPaint;
    private Paint buttonTextPaint;
    private String displayWord;
    private RectF nextButtonRect;
    private RectF submitButtonRect;
    private String[][] wordmas = {
            {"APPLE", "BREAD", "CHAIR", "DREAM", "EARTH", "FLAME", "GRAPE", "HOUSE", "ICING", "JUICE"},
            {"KNIFE", "LEMON", "MUSIC", "NIGHT", "OCEAN", "PEARL", "QUEEN", "RIVER", "SUGAR", "TIGER"},
            {"UNCLE", "VOICE", "WATER", "YOUTH", "ZEBRA", "ANGEL", "BEACH", "CLOCK", "DANCE", "EAGLE"},
            {"FRUIT", "GHOST", "HONEY", "IMAGE", "JEWEL", "KOALA", "LIGHT", "MANGO", "NOVEL", "OLIVE"},
            {"PIANO", "QUIET", "RULER", "SNAKE", "TABLE", "UMBRA", "VAPOR", "WHALE", "XEROX", "YACHT"},
            {"ABOVE", "BRAVE", "CRISP", "DRAFT", "EMPTY", "FRESH", "GIANT", "HOBBY", "IDEAL", "JELLY"},
            {"KNEAD", "LUCKY", "MAGIC", "NOBLE", "ORBIT", "PAINT", "QUICK", "RUSTY", "SPOON", "TRAIN"},
            {"URBAN", "VITAL", "WRECK", "YACHT", "ZESTY", "ALARM", "BLOOM", "CLOUD", "DITCH", "ERUPT"},
            {"FLASH", "GRIND", "HASTE", "INBOX", "JUDGE", "KNEEL", "LODGE", "MIDST", "NOTCH", "ORBIT"},
            {"PLUME", "QUEST", "ROAST", "SWIFT", "TROOP", "USHER", "VERGE", "WHARF", "YARNS", "ZILCH"}};
    private String[] wordList;
    private String[] unsortWordList;
    private String word;
    private int currentWordIndex = 0;
    private boolean isProcessingTouch = false;
    private boolean isSolved = false;

    private Paint indicatorPaint;
    private Paint indicatorBorderPaint;
    private RectF indicatorRect;
    private int ID;


    public ModuleWords(int row, int col, int correctID) {
        super(row, col);
        this.ID = correctID;
        this.setName("WordDisplay");
        initializePaints();
        setCellOccupied(3,3,true);
        setCellOccupied(3,0,true);
        setCellOccupied(3,2,true);
        setCellOccupied(2,2,true);
        setCellOccupied(3,1,true);
        setCellOccupied(2,1,true);
        nextButtonRect = new RectF();
        submitButtonRect = new RectF();
        wordList = getRandomLine();
        addRandomObjects();
        this.displayWord = wordList[0];
        Log.d("myLog", "ModuleWordDisplay: added with word '" + displayWord + "'");
    }

    private void initializePaints() {
        displayPaint = new Paint();
        displayPaint.setColor(Color.rgb(20, 20, 20));
        displayPaint.setStyle(Paint.Style.FILL);
        displayPaint.setAntiAlias(true);

        displayBorderPaint = new Paint();
        displayBorderPaint.setColor(Color.rgb(100, 100, 100));
        displayBorderPaint.setStyle(Paint.Style.STROKE);
        displayBorderPaint.setStrokeWidth(3);
        displayBorderPaint.setAntiAlias(true);

        wordPaint = new Paint();
        wordPaint.setColor(Color.rgb(0, 255, 0));
        wordPaint.setTextSize(90);
        wordPaint.setTextAlign(Paint.Align.CENTER);
        wordPaint.setAntiAlias(true);
        wordPaint.setTypeface(FontManager.getFont(FontManager.FONT_SEVEN_SEGMENT));

        // Paints для кнопок
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.rgb(50, 50, 50));
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setAntiAlias(true);

        buttonBorderPaint = new Paint();
        buttonBorderPaint.setColor(Color.rgb(150, 150, 150));
        buttonBorderPaint.setStyle(Paint.Style.STROKE);
        buttonBorderPaint.setStrokeWidth(4);
        buttonBorderPaint.setAntiAlias(true);

        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(60);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setAntiAlias(true);
        buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        indicatorPaint = new Paint();
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setAntiAlias(true);

        indicatorBorderPaint = new Paint();
        indicatorBorderPaint.setColor(Color.rgb(150, 150, 150));
        indicatorBorderPaint.setStyle(Paint.Style.FILL);
        indicatorBorderPaint.setStrokeWidth(2);
        indicatorBorderPaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds) {
        super.draw(canvas, bounds);

        if (canvas == null || bounds == null) return;

        float displayWidth = bounds.width() * 0.45f;
        float displayHeight = bounds.height() * 0.2f;
        float displayLeft = bounds.centerX() - displayWidth / 2;
        float displayTop = bounds.top + bounds.height() * 0.65f;

        RectF displayRect = new RectF(displayLeft, displayTop, displayLeft + displayWidth, displayTop + displayHeight);
        canvas.drawRoundRect(displayRect, 10, 10, displayPaint);
        canvas.drawRoundRect(displayRect, 10, 10, displayBorderPaint);

        float textX = displayRect.centerX();
        float textY = displayRect.centerY() - (wordPaint.descent() + wordPaint.ascent()) / 2;
        canvas.drawText(displayWord, textX, textY, wordPaint);

        // Кнопки под дисплеем
        float buttonWidth = bounds.width() * 0.3f;
        float buttonHeight = bounds.height() * 0.1f;
        float buttonSpacing = bounds.width() * 0.3f;

        float buttonsTop = bounds.bottom - bounds.height() * 0.13f;

        float nextLeft = bounds.centerX() - buttonWidth - buttonSpacing / 2;
        float submitLeft = bounds.centerX() + buttonSpacing / 2;

        nextButtonRect.set(nextLeft, buttonsTop, nextLeft + buttonWidth, buttonsTop + buttonHeight);
        submitButtonRect.set(submitLeft, buttonsTop, submitLeft + buttonWidth, buttonsTop + buttonHeight);

        // Рисуем NEXT
        canvas.drawRoundRect(nextButtonRect, 15, 15, buttonPaint);
        canvas.drawRoundRect(nextButtonRect, 15, 15, buttonBorderPaint);
        float nextTextY = nextButtonRect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2;
        canvas.drawText("NEXT", nextButtonRect.centerX(), nextTextY, buttonTextPaint);

        // Рисуем SUBMIT
        canvas.drawRoundRect(submitButtonRect, 15, 15, buttonPaint);
        canvas.drawRoundRect(submitButtonRect, 15, 15, buttonBorderPaint);
        float submitTextY = submitButtonRect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2;
        canvas.drawText("SUBMIT", submitButtonRect.centerX(), submitTextY, buttonTextPaint);

        float indicatorRadius = bounds.width() * 0.06f;
        float indicatorX = bounds.centerX();
        float indicatorY = buttonsTop + buttonHeight / 2;

        if (isSolved) {
            // Зеленый, если решено
            indicatorPaint.setColor(Color.rgb(0, 255, 0)); // Яркий зеленый
        } else {
            // Тусклый оранжевый, если не решено
            indicatorPaint.setColor(Color.rgb(255, 165, 50)); // Оранжевый с уменьшенной яркостью
        }

        // Рисуем индикатор
        canvas.drawCircle(indicatorX, indicatorY, indicatorRadius, indicatorBorderPaint);
        canvas.drawCircle(indicatorX, indicatorY, indicatorRadius - 5f, indicatorPaint);
    }

    @Override
    public boolean handleTouch(float x, float y, float moduleWidth, float moduleHeight) {
        super.handleTouch(x, y, moduleWidth, moduleHeight);
        if (isProcessingTouch) {
            return true;
        }
        float buttonWidth = moduleWidth * 0.35f;
        float buttonHeight = moduleHeight * 0.15f;
        float buttonSpacing = moduleWidth * 0.1f;
        float buttonsTop = moduleHeight - moduleHeight * 0.15f - 20;

        float nextLeft = moduleWidth / 2 - buttonWidth - buttonSpacing / 2;
        float submitLeft = moduleWidth / 2 + buttonSpacing / 2;

        RectF nextButtonRectTemp = new RectF(nextLeft, buttonsTop, nextLeft + buttonWidth, buttonsTop + buttonHeight);
        RectF submitButtonRectTemp = new RectF(submitLeft, buttonsTop, submitLeft + buttonWidth, buttonsTop + buttonHeight);

        boolean handled = false;

        if (nextButtonRectTemp.contains(x, y)) {
            isProcessingTouch = true; // начинаем обработку
            currentWordIndex = (currentWordIndex + 1) % wordList.length;
            displayWord = wordList[currentWordIndex];
            Log.d("myLog", "NEXT pressed -> word: " + displayWord);
            handled = true;
        } else if (submitButtonRectTemp.contains(x, y)) {
            isProcessingTouch = true;
            Log.d("myLog", "SUBMIT pressed with word: " + displayWord);
            if (ID == currentWordIndex){
                isSolved = true;
            }
            handled = true;
        }

        if (handled) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                isProcessingTouch = false;
            }, 300);
        }

        return handled;
    }
    public void reset(){
        wordList = getRandomLine();
        this.displayWord = wordList[0];
    }

    private String[] getRandomLine() {
        java.util.Random random = new java.util.Random();

        if (random.nextBoolean()) {
            int row = random.nextInt(wordmas.length);
            String[] result = wordmas[row].clone();
            unsortWordList = result;
            shuffleArray(result);
            return result;
        } else {
            int col = random.nextInt(wordmas[0].length);
            String[] result = new String[wordmas.length];

            for (int i = 0; i < wordmas.length; i++) {
                result[i] = wordmas[i][col];
            }
            unsortWordList = result;
            shuffleArray(result);
            return result;
        }
    }
    private void shuffleArray(String[] array) {
        java.util.Random random = new java.util.Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // Меняем местами
            String temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
    @Override
    public void update() {
        super.update();

    }
    @Override
    public boolean isSolved() {
        return super.isSolved();
    }

    public String getDisplayWord() {
        return displayWord;
    }

    public void setDisplayWord(String word) {
        this.displayWord = word.toUpperCase();
    }
    public void setCorrectID(int id){
        for (int i = 0; i < wordmas.length; i++) {
            if (wordList[ID] == unsortWordList[i]){
                this.ID = i;
            }
            //Log.d("myLog", "the word is: " + wordList[ID]);
        }
    }
}