package com.minesweeper.game;

import lombok.Data;

@Data
public class Cell {
    private boolean mine;
    private boolean revealed;
    private int adjacentMines;

    public boolean isMine() { return mine; }
    public void setMine(boolean mine) { this.mine = mine; }

    public boolean isRevealed() { return revealed; }
    public void reveal() { this.revealed = true; }

    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int adjacentMines) { this.adjacentMines = adjacentMines; }
}
