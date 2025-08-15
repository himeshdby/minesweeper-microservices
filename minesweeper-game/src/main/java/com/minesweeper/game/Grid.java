package com.minesweeper.game;

import java.util.*;

public class Grid {
    private final int size;
    private final Cell[][] cells;
    private final int numMines;
    private final Random rng;
    private boolean minesPlaced = false;


    public Grid(int size, int numMines, Random rng) {
        this.size = size;
        this.numMines = numMines;
        this.rng = rng;
        this.cells = new Cell[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c] = new Cell();
            }
        }
        // Mines are NOT placed here. They are placed on the first click.
    }

    public boolean areMinesPlaced() {
        return minesPlaced;
    }

    // For demo/tests: set exact mine coordinates (list of {row, col}).
    public void setMines(List<int[]> mineCoords) {
        if (mineCoords == null) throw new IllegalArgumentException("mineCoords cannot be null");
        if (mineCoords.size() != numMines) {
            throw new IllegalArgumentException("Expected " + numMines + " mines, got " + mineCoords.size());
        }
        clearMines();
        for (int[] rc : mineCoords) {
            int r = rc[0], c = rc[1];
            if (!inBounds(r, c)) {
                throw new IllegalArgumentException("Mine out of bounds at (" + r + "," + c + ")");
            }
            cells[r][c].setMine(true);
        }
        recomputeAdjacents();
        minesPlaced = true;
    }

    // Place mines after the first click. Prefer avoiding the clicked cell and its neighbors.
// If that’s not possible (small/dense board), fall back to avoiding only the clicked cell.
    public void placeMinesFirstClick(int safeR, int safeC) {
        boolean ok = placeMinesRandomAvoiding(safeR, safeC, true);
        if (!ok) {
            placeMinesRandomAvoiding(safeR, safeC, false);
        }
    }

    // Internal: place mines randomly, avoiding either the clicked cell (avoidNeighborhood=false)
// or the clicked cell and its neighbors (avoidNeighborhood=true). Returns true if placed.
    private boolean placeMinesRandomAvoiding(int safeR, int safeC, boolean avoidNeighborhood) {
        clearMines();

        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < size * size; i++) {
            int r = i / size;
            int c = i % size;
            if (avoidNeighborhood) {
                if (Math.abs(r - safeR) <= 1 && Math.abs(c - safeC) <= 1) continue;
            } else {
                if (r == safeR && c == safeC) continue;
            }
            positions.add(i);
        }

        if (positions.size() < numMines) {
            // Not enough room to place mines with this avoidance policy.
            clearMines();
            return false;
        }

        Collections.shuffle(positions, rng);
        for (int i = 0; i < numMines; i++) {
            int pos = positions.get(i);
            int r = pos / size;
            int c = pos % size;
            cells[r][c].setMine(true);
        }

        recomputeAdjacents();
        minesPlaced = true;
        return true;
    }

    private void clearMines() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c].setMine(false);
                cells[r][c].setAdjacentMines(0);
            }
        }
        minesPlaced = false;
    }

    private void recomputeAdjacents() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (cells[r][c].isMine()) {
                    cells[r][c].setAdjacentMines(0);
                    continue;
                }
                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (inBounds(nr, nc) && cells[nr][nc].isMine()) count++;
                    }
                }
                cells[r][c].setAdjacentMines(count);
            }
        }
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }

    public Cell getCell(int r, int c) {
        return cells[r][c];
    }

    public int getSize() { return size; }
    public int getNumMines() { return numMines; }

}
