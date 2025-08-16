package com.minesweeper.game;

import com.minesweeper.game.exception.InvalidMoveException;

import java.util.*;

/**
 * Complete Minesweeper game logic with random fixed mine generation:
 * - Generates a fixed random mine layout at construction based on grid size and numMines input.
 * - Applies mines lazily on the first reveal with optional first-click safety.
 * - Grid automatically recomputes adjacent counts when mines are set.
 * - Supports flood reveal for zero-adjacent cells.
 */
public class MinesweeperGame {
    private final Grid grid;
    private boolean gameOver;
    private int revealedCellsCount;

    // Fixed mine layout generated at construction
    private final List<int[]> fixedMines;
    // Controls whether first click is guaranteed safe
    private final boolean relocateMineOffFirstClick;

    // Default constructor: random RNG, safe first click
    public MinesweeperGame(int size, int numMines) {
        this(size, numMines, new Random(), true);
    }

    // Constructor with custom RNG: safe first click by default
    public MinesweeperGame(int size, int numMines, Random rng) {
        this(size, numMines, rng, true);
    }

    // Main constructor: generates random fixed mines based on input parameters
    public MinesweeperGame(int size, int numMines, Random rng, boolean relocateMineOffFirstClick) {
        // Clamp inputs to valid ranges
        if (size < 1) size = 1;
        int maxMines = size * size;
        if (numMines < 0) numMines = 0;
        if (numMines > maxMines) numMines = maxMines;

        this.grid = new Grid(size, numMines, rng);
        this.gameOver = false;
        this.revealedCellsCount = 0;
        this.relocateMineOffFirstClick = relocateMineOffFirstClick;

        // Generate fixed random mine layout immediately based on input
        this.fixedMines = generateRandomFixedMines(size, numMines, rng);
    }

    // Constructor with provided fixed layout
    public MinesweeperGame(int size, int numMines, Random rng, List<int[]> fixedMines, boolean relocateMineOffFirstClick) {
        if (size < 1) size = 1;
        int maxMines = size * size;
        if (numMines < 0) numMines = 0;
        if (numMines > maxMines) numMines = maxMines;

        this.grid = new Grid(size, numMines, rng);
        this.gameOver = false;
        this.revealedCellsCount = 0;
        this.fixedMines = fixedMines != null ? fixedMines : generateRandomFixedMines(size, numMines, rng);
        this.relocateMineOffFirstClick = relocateMineOffFirstClick;
    }

    // Constructor with provided fixed layout, safe first click by default
    public MinesweeperGame(int size, int numMines, Random rng, List<int[]> fixedMines) {
        this(size, numMines, rng, fixedMines, true);
    }

    /**
     * Display the game grid
     * @param showAll if true, shows all mines and numbers; if false, shows only revealed cells
     * @return formatted grid string
     */
    public String displayGrid(boolean showAll) {
        StringBuilder sb = new StringBuilder();

        // Column headers
        sb.append("  ");
        for (int c = 1; c <= grid.getSize(); c++) {
            sb.append(c).append(" ");
        }
        sb.append("\n");

        // Grid rows
        for (int r = 0; r < grid.getSize(); r++) {
            sb.append((char) ('A' + r)).append(" ");
            for (int c = 0; c < grid.getSize(); c++) {
                Cell cell = grid.getCell(r, c);
                if (showAll) {
                    // Show everything: mines as *, numbers as digits
                    sb.append(cell.isMine() ? "* " : cell.getAdjacentMines() + " ");
                } else if (cell.isRevealed()) {
                    // Show only revealed cells
                    sb.append(cell.getAdjacentMines()).append(" ");
                } else {
                    // Hidden cells
                    sb.append("_ ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Reveal a cell at the given position
     * @param position cell position like "A1", "B3", etc.
     * @return RevealResult containing mine hit status, adjacent count, and win status
     * @throws InvalidMoveException if position is invalid or game is over
     */
    public RevealResult revealCell(String position) throws InvalidMoveException {
        if (gameOver) {
            throw new InvalidMoveException("Game is already over.");
        }

        int[] rc = parsePosition(position);
        int r = rc[0], c = rc[1];

        // Apply fixed mines on first click (lazy placement)
        if (!grid.areMinesPlaced()) {
            List<int[]> layout = new ArrayList<>(fixedMines);

            // Optionally relocate mine off first-click cell for safety
            if (relocateMineOffFirstClick) {
                relocateOffFirstClickIfNeeded(layout, r, c, grid.getSize());
            }

            // Set mines and recompute adjacent counts
            grid.setMines(layout);
        }

        Cell cell = grid.getCell(r, c);

        // Check if mine hit
        if (cell.isMine()) {
            gameOver = true;
            return new RevealResult(true, -1, false);
        }

        // Check if already revealed
        if (cell.isRevealed()) {
            throw new InvalidMoveException("Cell already revealed");
        }

        // Reveal the cell
        cell.reveal();
        revealedCellsCount++;

        // Flood reveal if zero adjacent mines
        if (cell.getAdjacentMines() == 0) {
            floodReveal(r, c);
        }

        return new RevealResult(false, cell.getAdjacentMines(), isWin());
    }

    /**
     * Flood reveal using BFS to auto-reveal connected zero-adjacent cells
     */
    private void floodReveal(int r, int c) {
        Queue<int[]> queue = new ArrayDeque<>();
        boolean[][] enqueued = new boolean[grid.getSize()][grid.getSize()];

        queue.add(new int[]{r, c});
        enqueued[r][c] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0], col = current[1];

            // Check all 8 neighbors (including diagonals)
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue; // Skip self

                    int nr = row + dr, nc = col + dc;
                    if (!grid.inBounds(nr, nc)) continue;

                    Cell neighbor = grid.getCell(nr, nc);
                    if (neighbor.isMine() || neighbor.isRevealed()) continue;

                    // Reveal the neighbor
                    neighbor.reveal();
                    revealedCellsCount++;

                    // If neighbor is also zero-adjacent, add to queue for further expansion
                    if (neighbor.getAdjacentMines() == 0 && !enqueued[nr][nc]) {
                        enqueued[nr][nc] = true;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }
    }

    /**
     * Check if game is over (mine hit)
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Check if game is won (all non-mine cells revealed)
     */
    public boolean isWin() {
        int totalCells = grid.getSize() * grid.getSize();
        return revealedCellsCount == (totalCells - grid.getNumMines());
    }

    /**
     * Parse position string like "A1" into row/column coordinates
     */
    private int[] parsePosition(String position) throws InvalidMoveException {
        if (position == null) {
            throw new InvalidMoveException("Position is null.");
        }

        position = position.trim();
        if (position.length() < 2) {
            throw new InvalidMoveException("Invalid position format. Use e.g., A1.");
        }

        char rowChar = Character.toUpperCase(position.charAt(0));
        if (rowChar < 'A' || rowChar > 'Z') {
            throw new InvalidMoveException("Invalid row letter.");
        }
        int r = rowChar - 'A';

        int c;
        try {
            c = Integer.parseInt(position.substring(1).trim()) - 1; // Convert to 0-based
        } catch (NumberFormatException e) {
            throw new InvalidMoveException("Invalid column number.");
        }

        if (!grid.inBounds(r, c)) {
            throw new InvalidMoveException("Position out of grid bounds.");
        }

        return new int[]{r, c};
    }

    /**
     * Generate a fixed random set of unique mine coordinates based on grid size and numMines
     * @param size grid size (size x size)
     * @param numMines number of mines to place
     * @param rng random number generator
     * @return list of mine coordinates as int[]{row, col}
     */
    private static List<int[]> generateRandomFixedMines(int size, int numMines, Random rng) {
        int totalCells = size * size;

        // Clamp numMines to valid range
        if (numMines < 0) numMines = 0;
        if (numMines > totalCells) numMines = totalCells;

        // Create list of all possible positions
        List<Integer> positions = new ArrayList<>(totalCells);
        for (int i = 0; i < totalCells; i++) {
            positions.add(i);
        }

        // Shuffle and select first numMines positions
        Collections.shuffle(positions, rng);

        List<int[]> mines = new ArrayList<>(numMines);
        for (int i = 0; i < numMines; i++) {
            int pos = positions.get(i);
            int row = pos / size;
            int col = pos % size;
            mines.add(new int[]{row, col});
        }

        return mines;
    }

    /**
     * Relocate a mine away from the first-click cell if it exists there
     * @param mines list of mine coordinates
     * @param r first-click row
     * @param c first-click column
     * @param size grid size
     */
    private static void relocateOffFirstClickIfNeeded(List<int[]> mines, int r, int c, int size) {
        // Check if there's a mine at the first-click position
        boolean removed = false;
        Iterator<int[]> iterator = mines.iterator();
        while (iterator.hasNext()) {
            int[] mine = iterator.next();
            if (mine[0] == r && mine[1] == c) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (!removed) return; // No mine to relocate

        // Find first free cell to place the relocated mine
        outer:
        for (int rr = 0; rr < size; rr++) {
            for (int cc = 0; cc < size; cc++) {
                if (rr == r && cc == c) continue; // Skip first-click cell

                // Check if this position is already occupied by a mine
                boolean occupied = false;
                for (int[] mine : mines) {
                    if (mine[0] == rr && mine[1] == cc) {
                        occupied = true;
                        break;
                    }
                }

                if (!occupied) {
                    mines.add(new int[]{rr, cc});
                    break outer;
                }
            }
        }
    }

    /**
     * Result object returned by revealCell method
     */
    public static class RevealResult {
        public final boolean mineHit;      // true if a mine was detonated
        public final int adjacentMines;   // number of adjacent mines (-1 if mine hit)
        public final boolean win;          // true if game is won

        public RevealResult(boolean mineHit, int adjacentMines, boolean win) {
            this.mineHit = mineHit;
            this.adjacentMines = adjacentMines;
            this.win = win;
        }
    }

    // Getter methods for testing/debugging
    public Grid getGrid() { return grid; }
    public List<int[]> getFixedMines() { return new ArrayList<>(fixedMines); }
    public int getRevealedCellsCount() { return revealedCellsCount; }
    public boolean isRelocateMineOffFirstClick() { return relocateMineOffFirstClick; }
}
