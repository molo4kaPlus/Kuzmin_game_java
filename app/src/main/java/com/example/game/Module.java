package com.example.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Module {
    private String name;
    private boolean isActive;
    private int row;
    private int col;
    private int gridRows = 4;
    private int gridCols = 4;
    private int[][] occupancyGrid;
    private float cellSize;

    private Paint modulePaint;
    private Paint textPaint;
    private Paint activePaint;
    private Paint gridLinePaint;
    private Paint occupiedCellPaint;
    private Paint emptyCellPaint;
    private Paint objPaint;
    private List<Obj> objects;

    public Module(int row, int col) {
        this.name = "NONE";
        this.row = row;
        this.col = col;
        this.isActive = false;
        occupancyGrid = new int[gridRows][gridCols];
        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols; j++) {
                occupancyGrid[i][j] = 0;
            }
        }
        this.objects = new ArrayList<>();
        initializePaints();
    }

    public void addRandomObjects() {
        List<int[]> freeCells = new ArrayList<>();
        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                if (!isCellOccupied(r, c)) {
                    freeCells.add(new int[]{r, c});
                }
            }
        }

        if (freeCells.size() >= 4) {
            Collections.shuffle(freeCells);

            for (int i = 0; i < 4; i++) {
                int[] pos = freeCells.get(i);
                addObject(new ObjWire(), pos[0], pos[1]);
                Log.d("myLog", "Wire added to cell (" + pos[0] + "," + pos[1] + ")");
            }

            for (int i = 4; i < freeCells.size(); i++) {
                int[] pos = freeCells.get(i);
                addObject(new ObjBattery(pos[0], pos[1], "AA"), pos[0], pos[1]);
                Log.d("myLog", "Battery added to cell (" + pos[0] + "," + pos[1] + ")");
            }
        } else {
            Log.w("myLog", "Not enough free cells for 3 wires!");
        }
    }
    private void initializePaints() {
        modulePaint = new Paint();
        modulePaint.setColor(Color.rgb(60,60,60));
        modulePaint.setStyle(Paint.Style.FILL);
        modulePaint.setAntiAlias(true);

        activePaint = new Paint();
        activePaint.setColor(Color.rgb(50, 50, 50));
        activePaint.setStyle(Paint.Style.FILL);
        activePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.rgb(100, 100, 100));
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setStrokeWidth(1);
        gridLinePaint.setAntiAlias(true);

        occupiedCellPaint = new Paint();
        occupiedCellPaint.setColor(Color.rgb(255, 100, 100));
        occupiedCellPaint.setStyle(Paint.Style.FILL);
        occupiedCellPaint.setAntiAlias(true);
        occupiedCellPaint.setAlpha(150);

        emptyCellPaint = new Paint();
        emptyCellPaint.setColor(Color.rgb(100, 255, 100));
        emptyCellPaint.setStyle(Paint.Style.FILL);
        emptyCellPaint.setAntiAlias(true);
        emptyCellPaint.setAlpha(80);

        objPaint = new Paint();
        objPaint.setColor(Color.rgb(255, 200, 100));
        objPaint.setStyle(Paint.Style.FILL);
        objPaint.setAntiAlias(true);
    }
    public void draw(Canvas canvas, RectF bounds) {
        Paint currentPaint = isActive ? activePaint : modulePaint;
        canvas.drawRect(bounds, currentPaint);

        if (name != null && !name.isEmpty()) {
            float textY = bounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2);
            //drawInternalGrid(canvas, bounds);
            //canvas.drawText(name, bounds.centerX(), textY, textPaint);
            drawObjects(canvas, bounds);
        }
    }
    private void drawObjects(Canvas canvas, RectF bounds) {
        if (objects.isEmpty()) return;

        float gridLeft = bounds.left;
        float gridTop = bounds.top;

        cellSize = bounds.width() / gridCols;
        float totalGridWidth = cellSize * gridCols;
        float totalGridHeight = cellSize * gridRows;

        gridLeft = bounds.left;
        gridTop = bounds.top + (bounds.height() - totalGridHeight) / 2;

        if (totalGridHeight > bounds.height()) {
            cellSize = bounds.height() / gridRows;
            totalGridWidth = cellSize * gridCols;
            totalGridHeight = cellSize * gridRows;
            gridLeft = bounds.left + (bounds.width() - totalGridWidth) / 2;
            gridTop = bounds.top;
        }

        for (Obj obj : objects) {
            int row = obj.getGridRow();
            int col = obj.getGridCol();

            if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
                float cellLeft = gridLeft + col * cellSize;
                float cellTop = gridTop + row * cellSize;

                RectF objBounds = new RectF(
                        cellLeft + cellSize * 0.05f,
                        cellTop + cellSize * 0.05f,
                        cellLeft + cellSize * 0.95f,
                        cellTop + cellSize * 0.95f
                );

                obj.draw(canvas, objBounds);
            }
        }
    }
    public boolean handleTouch(float x, float y, float moduleWidth, float moduleHeight) {
        if (objects.isEmpty()) return false;
        float gridLeft = 0;
        float gridTop = 0;
        float cellSize = moduleWidth / gridCols;
        float totalGridHeight = cellSize * gridRows;

        gridTop = (moduleHeight - totalGridHeight) / 2;

        if (totalGridHeight > moduleHeight) {
            cellSize = moduleHeight / gridRows;
            float totalGridWidth = cellSize * gridCols;
            totalGridHeight = cellSize * gridRows;
            gridLeft = (moduleWidth - totalGridWidth) / 2;
            gridTop = 0;
        }
        int col = (int) ((x - gridLeft) / cellSize);
        int row = (int) ((y - gridTop) / cellSize);

        if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
            Obj obj = getObjectAt(row, col);
            if (obj != null) {
                obj.onClick();
                return true;
            }
        }

        return false;
    }
    private void drawInternalGrid(Canvas canvas, RectF bounds) {
        float gridLeft = bounds.left;
        float gridTop = bounds.top;

        cellSize = bounds.width() / gridCols;
        float totalGridWidth = cellSize * gridCols;
        float totalGridHeight = cellSize * gridRows;

        gridLeft = bounds.left;
        gridTop = bounds.top + (bounds.height() - totalGridHeight) / 2;

        if (totalGridHeight > bounds.height()) {
            cellSize = bounds.height() / gridRows;
            totalGridWidth = cellSize * gridCols;
            totalGridHeight = cellSize * gridRows;
            gridLeft = bounds.left + (bounds.width() - totalGridWidth) / 2;
            gridTop = bounds.top;
        }

        Paint gridBgPaint = new Paint();
        gridBgPaint.setColor(Color.rgb(40, 40, 40));
        gridBgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(gridLeft, gridTop,
                gridLeft + totalGridWidth,
                gridTop + totalGridHeight,
                gridBgPaint);

        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols; j++) {
                float cellLeft = gridLeft + j * cellSize;
                float cellTop = gridTop + i * cellSize;

                if (occupancyGrid[i][j] == 1) {
                    canvas.drawRect(cellLeft, cellTop,
                            cellLeft + cellSize,
                            cellTop + cellSize,
                            occupiedCellPaint);
                } else {
                    canvas.drawRect(cellLeft, cellTop,
                            cellLeft + cellSize,
                            cellTop + cellSize,
                            emptyCellPaint);
                }

                canvas.drawRect(cellLeft, cellTop,
                        cellLeft + cellSize,
                        cellTop + cellSize,
                        gridLinePaint);
            }
        }

        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.rgb(150, 150, 150));
        labelPaint.setTextSize(12);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Grid",
                gridLeft + (cellSize * gridCols) / 2,
                gridTop - 5,
                labelPaint);
    }
    public void addObject(Obj obj, int row, int col) {
        if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
            obj.setPosition(row, col);
            objects.add(obj);
            setCellOccupied(row, col, true);
        }
    }
    public void updateObjects() {
        for (Obj obj : objects) {
            obj.update();
        }
    }
    public boolean addObjectAuto(Obj obj) {
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                if (!isCellOccupied(row, col)) {
                    addObject(obj, row, col);
                    return true;
                }
            }
        }
        return false;
    }
    public void removeObject(int row, int col) {
        Obj toRemove = null;
        for (Obj obj : objects) {
            if (obj.getGridRow() == row && obj.getGridCol() == col) {
                toRemove = obj;
                break;
            }
        }

        if (toRemove != null) {
            objects.remove(toRemove);
            setCellOccupied(row, col, false);
        }
    }
    public void removeObject(int index) {
        if (index >= 0 && index < objects.size()) {
            Obj obj = objects.get(index);
            setCellOccupied(obj.getGridRow(), obj.getGridCol(), false);
            objects.remove(index);
        }
    }
    public Obj getObjectAt(int row, int col) {
        for (Obj obj : objects) {
            if (obj.getGridRow() == row && obj.getGridCol() == col) {
                return obj;
            }
        }
        return null;
    }
    public List<Obj> getObjects() {
        return objects;
    }
    public void clearObjects() {
        for (Obj obj : objects) {
            setCellOccupied(obj.getGridRow(), obj.getGridCol(), false);
        }
        objects.clear();
    }
    public boolean canPlaceObject(int row, int col) {
        return row >= 0 && row < gridRows &&
                col >= 0 && col < gridCols &&
                !isCellOccupied(row, col);
    }
    public void setCellOccupied(int row, int col, boolean occupied) {
        if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
            occupancyGrid[row][col] = occupied ? 1 : 0;
        }
    }
    public boolean isCellOccupied(int row, int col) {
        if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
            return occupancyGrid[row][col] == 1;
        }
        return false;
    }
    public void clearAllCells() {
        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols; j++) {
                occupancyGrid[i][j] = 0;
            }
        }
    }
    public void setAllCells(boolean occupied) {
        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols; j++) {
                occupancyGrid[i][j] = occupied ? 1 : 0;
            }
        }
    }
    public void setCustomGrid(int[][] customGrid) {
        if (customGrid.length == gridRows && customGrid[0].length == gridCols) {
            for (int i = 0; i < gridRows; i++) {
                for (int j = 0; j < gridCols; j++) {
                    occupancyGrid[i][j] = customGrid[i][j];
                }
            }
        }
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
    public Paint getActivePaint() {
        return activePaint;
    }
    public void update() {
        updateObjects();
    }
}
