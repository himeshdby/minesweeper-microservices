// FileName: MultipleFiles/MinesweeperGame.java
package com.minesweeper.game;

import com.minesweeper.game.exception.InvalidMoveException;

import java.util.*;

public class MinesweeperGame {
    private final Grid grid;
    private boolean gameOver;
    private int revealedCellsCount;
    private final List<int[]> fixedMines;
    private boolean relocateMineOffFirstClick;// Track revealed non-mine cells

    public MinesweeperGame(int size, int numMines) {
        this(size, numMines, new Random(), null);
    }


    public MinesweeperGame(int size, int numMines, Random rng) {
        this(size, numMines, rng, null);
    }

    // fixedMines: list of {row, col} to force a specific layout
    public MinesweeperGame(int size, int numMines, Random rng, List<int[]> fixedMines) {
        if (size < 2) throw new IllegalArgumentException("Grid size must be >= 2");
        int maxMines = (int) Math.floor(size * size * 0.35);
        if (numMines < 1 || numMines > maxMines)
            throw new IllegalArgumentException("Invalid number of mines");
        this.grid = new Grid(size, numMines, rng);
        this.gameOver = false;
        this.revealedCellsCount = 0;
        this.fixedMines = fixedMines;
    }

    public MinesweeperGame(int i, int i1, Random fixedRng, List<int[]> fixed, boolean b) {
        this.grid = new Grid(i, i1, fixedRng);
        this.gameOver = false;
        this.revealedCellsCount = 0;
        this.fixedMines = fixed;
        this.relocateMineOffFirstClick=b;
    }

    public String displayGrid(boolean showAll) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        for (int c = 1; c <= grid.getSize(); c++) sb.append(c).append(" ");
        sb.append("\n");
        for (int r = 0; r < grid.getSize(); r++) {
            sb.append((char) ('A' + r)).append(" ");
            for (int c = 0; c < grid.getSize(); c++) {
                Cell cell = grid.getCell(r, c);
                if (showAll) {
                    sb.append(cell.isMine() ? "* " : cell.getAdjacentMines() + " ");
                } else if (cell.isRevealed()) {
                    sb.append(cell.getAdjacentMines()).append(" ");
                } else {
                    sb.append("_ ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public RevealResult revealCell(String position) throws InvalidMoveException {
        if (gameOver) throw new InvalidMoveException("Game is already over.");

        int[] rc = parsePosition(position);
        int r = rc[0], c = rc[1];

        // Lazily place mines on the first click
        if (!grid.areMinesPlaced()) {
            if (fixedMines != null) {
                // Ensure first cell isn’t a mine in fixed layouts: relocate if necessary
                List<int[]> adjusted = new ArrayList<>();
                boolean moved = false;
                for (int[] m : fixedMines) {
                    if (m[0] == r && m[1] == c) { moved = true; continue; }
                    adjusted.add(m);
                }
                if (moved) {
                    outer:
                    for (int rr = 0; rr < grid.getSize(); rr++) {
                        for (int cc = 0; cc < grid.getSize(); cc++) {
                            if (rr == r && cc == c) continue;
                            boolean used = false;
                            for (int[] t : adjusted) { if (t[0] == rr && t[1] == cc) { used = true; break; } }
                            if (!used) { adjusted.add(new int[]{rr, cc}); break outer; }
                        }
                    }
                }
                grid.setMines(adjusted);
            } else {
                grid.placeMinesFirstClick(r, c); // first click is safe; try to make it 0-adjacent
            }
        }

        Cell cell = grid.getCell(r, c);
        if (cell.isMine()) { // should not happen on first click now
            gameOver = true;
            return new RevealResult(true, -1, false);
        }
        if (cell.isRevealed()) throw new InvalidMoveException("Cell already revealed");

        cell.reveal();
        revealedCellsCount++;

        if (cell.getAdjacentMines() == 0) {
            floodReveal(r, c);
        }

        return new RevealResult(false, cell.getAdjacentMines(), isWin());
    }

    private void floodReveal(int r, int c) {
        Queue<int[]> q = new ArrayDeque<>();
        boolean[][] enqueued = new boolean[grid.getSize()][grid.getSize()];
        q.add(new int[]{r, c});
        enqueued[r][c] = true;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int row = cur[0], col = cur[1];

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr, nc = col + dc;
                    if (!grid.inBounds(nr, nc)) continue;

                    Cell neigh = grid.getCell(nr, nc);
                    if (neigh.isMine() || neigh.isRevealed()) continue;

                    neigh.reveal();
                    revealedCellsCount++;

                    if (neigh.getAdjacentMines() == 0 && !enqueued[nr][nc]) {
                        enqueued[nr][nc] = true;
                        q.add(new int[]{nr, nc});
                    }
                }
            }
        }
    }

    public boolean isGameOver() { return gameOver; }

    public boolean isWin() {
        int total = grid.getSize() * grid.getSize();
        return revealedCellsCount == (total - grid.getNumMines());
    }

    private int[] parsePosition(String position) throws InvalidMoveException {
        if (position == null) throw new InvalidMoveException("Position is null.");
        position = position.trim();
        if (position.length() < 2) {
            throw new InvalidMoveException("Invalid position format. Use e.g., A1.");
        }
        char rowChar = Character.toUpperCase(position.charAt(0));
        if (rowChar < 'A' || rowChar > 'Z') throw new InvalidMoveException("Invalid row letter.");
        int r = rowChar - 'A';

        int c;
        try {
            c = Integer.parseInt(position.substring(1).trim()) - 1;
        } catch (NumberFormatException e) {
            throw new InvalidMoveException("Invalid column number.");
        }

        if (!grid.inBounds(r, c)) throw new InvalidMoveException("Position out of grid bounds.");
        return new int[]{r, c};
    }

    public static class RevealResult {
        public final boolean mineHit;
        public final int adjacentMines;
        public final boolean win;

        public RevealResult(boolean mineHit, int adjacentMines, boolean win) {
            this.mineHit = mineHit;
            this.adjacentMines = adjacentMines;
            this.win = win;
        }
    }
}
