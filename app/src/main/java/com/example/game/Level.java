package com.example.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Level {
    //region screen vars
    private int gridX;
    private int gridY;
    private int bombW;
    private int bombH;
    private int cellSize;
    private int screenW;
    private int screenH;
    private int bombX;
    private int bombY;
    //endregion of screen vars
    //region game vars
    private int[][] cells;
    private List<Module> modules;
    private boolean bombNeedsRedraw = true;
    private Bitmap cachedBombBitmap;
    //endregion of game vars
    //region paint vars
    private Paint bombPaint;
    private Paint gridPaint;
    private Paint moduleCellPaint;
    private Paint borderPaint;
    Paint layerPaint = new Paint();
    private RectF cachedBombRect;
    private RectF[] cachedLayerRects;
    //endregion of paint vars

    public Level(int gridX, int gridY){
        this.gridX = gridX;
        this.gridY = gridY;
        this.cells = new int[gridX][gridY];
        this.modules = new ArrayList<>();
        initializePaints();
        createStandardModules();
    }

    private void createStandardModules() {
        int index = 0;
        for (int row = 0; row < gridY; row++) {
            for (int col = 0; col < gridX; col++) {
                Module module = new Module(row, col);
                modules.add(module);
            }
        }
    }
    //region logic
    public void addModule(String type,int row, int col, Object... params) {
        if (row >= 0 && row < gridY && col >= 0 && col < gridX) {
            Module module = ModuleRegistry.createModule(type, row, col, params);
            modules.removeIf(m -> m.getRow() == row && m.getCol() == col);
            modules.add(module);
        }
    }

    //endregion logic
    //region draw
    public void draw(Canvas canvas){
        if (canvas == null) {
            Log.d("myLog", "levelDraw: Could not load canvas!");
            return;
        }
        if (bombNeedsRedraw) {
            updateBombBitmap(canvas);
            bombNeedsRedraw = false;
        }
        if (cachedBombBitmap != null) {
            canvas.drawBitmap(cachedBombBitmap, bombX, bombY, null);
        }
        drawModulesOnTop(canvas);
    }
    private void updateBombBitmap(Canvas canvas) {
        if (cachedBombBitmap != null) {
            cachedBombBitmap.recycle();
        }
        if (bombW > 0 && bombH > 0) {
            try {
                cachedBombBitmap = Bitmap.createBitmap(bombW, bombH, Bitmap.Config.ARGB_8888);
                Canvas bombCanvas = new Canvas(cachedBombBitmap);

                // Рисуем бомбу на отдельном канвасе
                drawBombToBitmap(bombCanvas);
            } catch (Exception e) {
                Log.e("myLog", "Error creating bomb bitmap: " + e.getMessage());
                cachedBombBitmap = null;
            }
        }
    }
    private void drawBombToBitmap(Canvas canvas) {
        if (canvas == null || bombW <= 0 || bombH <= 0) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT);
        RectF bombRect = new RectF(0, 0, bombW, bombH);
        for (int i = 0; i < 5; i++) {
            RectF layerRect = new RectF(
                    bombRect.left + i * 3,
                    bombRect.top + i * 3,
                    bombRect.right - i * 3,
                    bombRect.bottom - i * 3
            );
            canvas.drawRoundRect(layerRect, 15, 15, layerPaint);
        }

        canvas.drawRoundRect(bombRect, 15, 15, bombPaint);
        canvas.drawRoundRect(bombRect, 15, 15, borderPaint);

        drawModuleGridToBitmap(canvas);
    }
    private void drawModuleGridToBitmap(Canvas canvas) {
        if (bombW <= 0 || bombH <= 0) return;

        int gridPadding = 40;
        int gridAreaWidth = bombW - 2 * gridPadding;
        int gridAreaHeight = bombH - 2 * gridPadding;
        int cellWidth = gridAreaWidth / gridX;
        int cellHeight = gridAreaHeight / gridY;
        int gridStartX = (bombW - cellWidth * gridX) / 2;
        int gridStartY = (bombH - cellHeight * gridY) / 2;

        for (int row = 0; row < gridY; row++) {
            for (int col = 0; col < gridX; col++) {
                int cellX = gridStartX + col * cellWidth;
                int cellY = gridStartY + row * cellHeight;
                RectF cellRect = new RectF(cellX, cellY, cellX + cellWidth, cellY + cellHeight);
                canvas.drawRect(cellRect, moduleCellPaint);
                canvas.drawRect(cellRect, gridPaint);
            }
        }
    }
    private void drawModulesOnTop(Canvas canvas) {
        if (bombW <= 0 || bombH <= 0) return;

        int gridPadding = 40;
        int gridAreaWidth = bombW - 2 * gridPadding;
        int gridAreaHeight = bombH - 2 * gridPadding;
        int cellWidth = gridAreaWidth / gridX;
        int cellHeight = gridAreaHeight / gridY;
        int gridStartX = bombX + (bombW - cellWidth * gridX) / 2;
        int gridStartY = bombY + (bombH - cellHeight * gridY) / 2;

        for (Module module : modules) {
            int row = module.getRow();
            int col = module.getCol();

            if (row < gridY && col < gridX) {
                int cellX = gridStartX + col * cellWidth;
                int cellY = gridStartY + row * cellHeight;
                RectF cellRect = new RectF(cellX, cellY, cellX + cellWidth, cellY + cellHeight);

                float modulePadding = 5f;
                RectF moduleRect = new RectF(
                        cellRect.left + modulePadding,
                        cellRect.top + modulePadding,
                        cellRect.right - modulePadding,
                        cellRect.bottom - modulePadding
                );

                module.draw(canvas, moduleRect);
            }
        }
    }
    public void requestBombRedraw() {
        bombNeedsRedraw = true;
    }
    public void setScreenDimensions(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;

        int padding = 50;
        int availableWidth = screenW - 2 * padding;
        int availableHeight = screenH - 2 * padding;

        int cellWidth = availableWidth / gridX;
        int cellHeight = availableHeight / gridY;
        this.cellSize = Math.min(cellWidth, cellHeight);
        calculateBombDimensions();
    }
    private void calculateBombDimensions() {
        final float BOMB_ASPECT_RATIO = 16.0f / 9.0f;

        int maxWidth = (int) (screenW * 0.85f);
        int maxHeight = (int) (screenH * 0.90f);

        float screenAspectRatio = (float) screenW / screenH;

        if (screenAspectRatio > BOMB_ASPECT_RATIO) {
            bombH = Math.min(maxHeight, (int) (maxWidth / BOMB_ASPECT_RATIO));
            bombW = (int) (bombH * BOMB_ASPECT_RATIO);
        } else {
            bombW = Math.min(maxWidth, (int) (maxHeight * BOMB_ASPECT_RATIO));
            bombH = (int) (bombW / BOMB_ASPECT_RATIO);
        }
        bombX = (screenW - bombW) / 2;
        bombY = (screenH - bombH) / 2;

        Log.d("myLog", String.format("Bomb: %dx%d at (%d,%d), Screen: %dx%d",
                bombW, bombH, bombX, bombY, screenW, screenH));

        if (cachedBombBitmap != null) {
            cachedBombBitmap.recycle();
            cachedBombBitmap = null;
        }
        requestBombRedraw();
    }
    public void cleanup() {
        if (cachedBombBitmap != null) {
            cachedBombBitmap.recycle();
            cachedBombBitmap = null;
        }
    }
    private void initializePaints() {
        bombPaint = new Paint();
        bombPaint.setColor(Color.rgb(40, 40, 40));
        bombPaint.setStyle(Paint.Style.FILL);
        bombPaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(Color.rgb(80, 80, 80));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2);
        gridPaint.setAntiAlias(true);

        moduleCellPaint = new Paint();
        moduleCellPaint.setColor(Color.rgb(30, 30, 30));
        moduleCellPaint.setStyle(Paint.Style.FILL);
        moduleCellPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setColor(Color.rgb(100, 100, 100));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5);
        borderPaint.setAntiAlias(true);

        layerPaint.setStyle(Paint.Style.FILL);
        layerPaint.setColor(Color.argb(20, 60, 60, 60));
    }
    //endregion draw
    public boolean handleTouch(float x, float y) {
        if (bombW <= 0 || bombH <= 0) return false;

        if (x < bombX || x > bombX + bombW || y < bombY || y > bombY + bombH) {
            return false;
        }

        int gridPadding = 40;
        int gridAreaWidth = bombW - 2 * gridPadding;
        int gridAreaHeight = bombH - 2 * gridPadding;
        int cellWidth = gridAreaWidth / gridX;
        int cellHeight = gridAreaHeight / gridY;
        int gridStartX = bombX + (bombW - cellWidth * gridX) / 2;
        int gridStartY = bombY + (bombH - cellHeight * gridY) / 2;

        int col = (int) ((x - gridStartX) / cellWidth);
        int row = (int) ((y - gridStartY) / cellHeight);

        if (row >= 0 && row < gridY && col >= 0 && col < gridX) {
            Module module = getModuleAt(row, col);
            if (module != null) {
                float moduleX = x - (gridStartX + col * cellWidth);
                float moduleY = y - (gridStartY + row * cellHeight);
                return module.handleTouch(moduleX, moduleY, cellWidth, cellHeight);
            }
        }

        return false;
    }


    //region getters
    public int getGridWidth() { return gridX; }
    public int getGridHeight() { return gridY; }
    public int getCellSize() { return cellSize; }

    public Module getModuleAt(int row, int col) {
        for (Module module : modules) {
            if (module.getRow() == row && module.getCol() == col) {
                return module;
            }
        }
        return null;
    }
    public List<Module> getModules() {
        return modules;
    }
    public float getTimeLeft(){
        float time = 0;
        for (Module module : modules) {
            if (module instanceof Module) {
                if (module.getName() == "Timer"){
                    time = ((ModuleTimer) module).getTimeLeft();
                    time = time / 1000f;
                }
            }
        }
        return time;
    }
    public boolean hasRJ45(){
        for (Module module : modules){
            if(module.hasRJ45()){
                return true;
            }
        }
        return false;
    }
    public boolean hasPShalf(){
        for (Module module : modules){
            if(module.hasPShalf){
                return true;
            }
        }
        return false;
    }
    public int getSerial(){
        return getModuleAt(0,0).serial;
    }
    //endregion getters
}