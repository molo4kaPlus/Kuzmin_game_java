package com.example.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class Obj {
    protected int gridRow;
    protected int gridCol;
    protected float rotation = 0;
    protected boolean isActive = false;
    public Obj() {
        this.gridRow = 0;
        this.gridCol = 0;
    }
    public Obj(int row, int col) {
        this.gridRow = row;
        this.gridCol = col;
    }
    public abstract void draw(Canvas canvas, RectF bounds);
    public void update() {
        // Базовая реализация пустая
        // Переопределить в дочерних классах при необходимости
    }
    public void onClick() {
        // Базовая реализация пустая
        // Переопределить в дочерних классах при необходимости
    }
    public int getGridRow() {
        return gridRow;
    }
    public void setGridRow(int gridRow) {
        this.gridRow = gridRow;
    }

    public int getGridCol() {
        return gridCol;
    }

    public void setGridCol(int gridCol) {
        this.gridCol = gridCol;
    }

    public void setPosition(int row, int col) {
        this.gridRow = row;
        this.gridCol = col;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public abstract String getType();
}