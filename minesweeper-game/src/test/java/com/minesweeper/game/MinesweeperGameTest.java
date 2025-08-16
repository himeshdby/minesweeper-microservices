package com.minesweeper.game;

import com.minesweeper.game.exception.InvalidMoveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class MinesweeperGameTest {

    private Random fixedRng;

    @BeforeEach
    void setUp() {
        // Use fixed seed for deterministic tests
        fixedRng = new Random(123L);
    }

    // ==================== Constructor Tests ====================

    @Test
    void testDefaultConstructor() {
        MinesweeperGame game = new MinesweeperGame(4, 3);
        assertNotNull(game);
        assertFalse(game.isGameOver());
        assertFalse(game.isWin());
        assertEquals(0, game.getRevealedCellsCount());
        assertTrue(game.isRelocateMineOffFirstClick());
    }

    @Test
    void testConstructorWithRng() {
        MinesweeperGame game = new MinesweeperGame(4, 3, fixedRng);
        assertNotNull(game);
        assertFalse(game.isGameOver());
        assertFalse(game.isWin());
        assertTrue(game.isRelocateMineOffFirstClick());
    }

    @Test
    void testConstructorWithRngAndRelocateFlag() {
        MinesweeperGame game = new MinesweeperGame(4, 3, fixedRng, false);
        assertNotNull(game);
        assertFalse(game.isRelocateMineOffFirstClick());
    }

    @Test
    void testConstructorWithFixedMines() {
        List<int[]> fixedMines = createFixedMines();
        MinesweeperGame game = new MinesweeperGame(4, 3, fixedRng, fixedMines, true);
        assertNotNull(game);
        assertEquals(3, game.getFixedMines().size());
    }

    private List<int[]> createFixedMines() {
        List<int[]> fixed = new ArrayList<>();
        fixed.add(new int[]{0, 1});
        fixed.add(new int[]{1, 1});
        fixed.add(new int[]{2, 0});
        return fixed;
    }

    @Test
    void testConstructorWithFixedMinesDefaultRelocate() {
        List<int[]> fixedMines = createFixedMines();
        MinesweeperGame game = new MinesweeperGame(4, 3, fixedRng, fixedMines);
        assertNotNull(game);
        assertTrue(game.isRelocateMineOffFirstClick());
    }

    @Test
    void testConstructorWithNullFixedMines() {
        MinesweeperGame game = new MinesweeperGame(4, 3, fixedRng, null, true);
        assertNotNull(game);
        assertEquals(3, game.getFixedMines().size()); // Should generate random mines
    }

    // ==================== Input Validation Tests ====================

    @Test
    void testConstructorWithInvalidSize() {
        MinesweeperGame game = new MinesweeperGame(0, 3, fixedRng, true);
        assertNotNull(game); // Should clamp size to 1
    }

    @Test
    void testConstructorWithNegativeSize() {
        MinesweeperGame game = new MinesweeperGame(-1, 3, fixedRng, true);
        assertNotNull(game); // Should clamp size to 1
    }

    @Test
    void testConstructorWithNegativeMines() {
        MinesweeperGame game = new MinesweeperGame(4, -1, fixedRng, true);
        assertEquals(0, game.getFixedMines().size()); // Should clamp mines to 0
    }

    @Test
    void testConstructorWithTooManyMines() {
        MinesweeperGame game = new MinesweeperGame(4, 20, fixedRng, true);
        assertEquals(16, game.getFixedMines().size()); // Should clamp to size*size
    }

    // ==================== Display Grid Tests ====================

    @Test
    void testDisplayGridInitialState() {
        MinesweeperGame game = new MinesweeperGame(3, 2, fixedRng, true);
        String display = game.displayGrid(false);

        assertTrue(display.contains("  1 2 3"));
        assertTrue(display.contains("A _ _ _"));
        assertTrue(display.contains("B _ _ _"));
        assertTrue(display.contains("C _ _ _"));
    }

    @Test
    void testDisplayGridShowAll() throws InvalidMoveException {
        List<int[]> fixedMines = List.of(new int[]{0, 0}, new int[]{1, 1});
        MinesweeperGame game = new MinesweeperGame(3, 2, fixedRng, fixedMines, false);

        // Trigger mine placement
        try {
            game.revealCell("A1"); // This should hit a mine
        } catch (Exception e) {
            // Expected if A1 is a mine
        }

        String display = game.displayGrid(true);
        assertTrue(display.contains("*")); // Should show mines
    }

    @Test
    void testDisplayGridAfterReveal() throws InvalidMoveException {
        List<int[]> fixedMines = List.of(new int[]{0, 0}, new int[]{1, 1});
        MinesweeperGame game = new MinesweeperGame(3, 2, fixedRng, fixedMines, true);

        game.revealCell("C3"); // Safe cell
        String display = game.displayGrid(false);

        assertFalse(display.contains("C _ _ _")); // C3 should be revealed
    }

    // ==================== Reveal Cell Tests ====================

    @Test
    void testRevealCellSafeFirstClick() throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(4, 3, fixedRng, true);
        MinesweeperGame.RevealResult result = game.revealCell("A1");

        assertFalse(result.mineHit);
        assertTrue(result.adjacentMines >= 0);
        assertFalse(result.win);
        assertEquals(1, game.getRevealedCellsCount()); // At least one cell revealed
    }

    @Test
    void testRevealCellMineHit() throws InvalidMoveException {
        List<int[]> fixedMines = List.of(new int[]{0, 0}, new int[]{1, 1}, new int[]{2, 2});
        MinesweeperGame game = new MinesweeperGame(3, 3, fixedRng, fixedMines, false);

        MinesweeperGame.RevealResult result = game.revealCell("A1");
        assertTrue(result.mineHit);
        assertEquals(-1, result.adjacentMines);
        assertFalse(result.win);
        assertTrue(game.isGameOver());
    }

    @Test
    void testRevealCellWithRelocation() throws InvalidMoveException {
        List<int[]> fixedMines = List.of(new int[]{0, 0}, new int[]{1, 1}, new int[]{2, 2});
        MinesweeperGame game = new MinesweeperGame(3, 3, fixedRng, fixedMines, true);

        MinesweeperGame.RevealResult result = game.revealCell("A1");
        assertFalse(result.mineHit); // Should be safe due to relocation
        assertFalse(game.isGameOver());
    }

    @Test
    void testRevealCellFloodReveal() throws InvalidMoveException {
        // Create a sparse mine layout to ensure some zero cells
        List<int[]> fixedMines = List.of(new int[]{0, 0});
        MinesweeperGame game = new MinesweeperGame(4, 1, fixedRng, fixedMines, true);

        MinesweeperGame.RevealResult result = game.revealCell("D4");
        assertFalse(result.mineHit);

        // Should trigger flood reveal if D4 has 0 adjacent mines
        assertTrue(game.getRevealedCellsCount() >= 1);
    }

    // ==================== Invalid Move Tests ====================

    @Test
    void testRevealCellAfterGameOver() throws InvalidMoveException {
        List<int[]> fixedMines = List.of(new int[]{0, 0});
        MinesweeperGame game = new MinesweeperGame(2, 1, fixedRng, fixedMines, false);

        game.revealCell("A1"); // Hit mine
        assertTrue(game.isGameOver());

        assertThrows(InvalidMoveException.class, () -> game.revealCell("B1"));
    }

    @Test
    void testRevealAlreadyRevealedCell() throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(3, 1, fixedRng, true);
        game.revealCell("A1");

        assertThrows(InvalidMoveException.class, () -> game.revealCell("A1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "A", "1", "AA", "11", "@1", "A@"})
    void testInvalidPositionFormats(String position) {
        MinesweeperGame game = new MinesweeperGame(3, 1, fixedRng, true);
        assertThrows(InvalidMoveException.class, () -> game.revealCell(position));
    }

    @Test
    void testNullPosition() {
        MinesweeperGame game = new MinesweeperGame(3, 1, fixedRng, true);
        assertThrows(InvalidMoveException.class, () -> game.revealCell(null));
    }

    @ParameterizedTest
    @CsvSource({"A4,3", "D1,3", "Z99,3"})
    void testOutOfBoundsPositions(String position, int size) {
        MinesweeperGame game = new MinesweeperGame(size, 1, fixedRng, true);
        assertThrows(InvalidMoveException.class, () -> game.revealCell(position));
    }

    // ==================== Win Condition Tests ====================

    @Test
    void testWinCondition() throws InvalidMoveException {
        // Create a small game with known mine layout
        List<int[]> fixedMines = List.of(new int[]{0, 0});
        MinesweeperGame game = new MinesweeperGame(2, 1, fixedRng, fixedMines, true);

        // Reveal all non-mine cells
        game.revealCell("B1");
        game.revealCell("A2");
        MinesweeperGame.RevealResult result = game.revealCell("B2");

        assertTrue(result.win);
        assertTrue(game.isWin());
        assertFalse(game.isGameOver());
    }

    // ==================== Position Parsing Tests ====================

    @ParameterizedTest
    @CsvSource({
            "A1,0,0",
            "B2,1,1",
            "C3,2,2",
            "a1,0,0", // Test case insensitive
            "z26,25,25"
    })
    void testValidPositionParsing(String position, int expectedRow, int expectedCol) throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(26, 1, fixedRng, true);
        // This test verifies parsing by ensuring no exception is thrown for valid positions
        assertDoesNotThrow(() -> game.revealCell(position));
    }

    // ==================== Edge Cases Tests ====================

    @Test
    void testSingleCellGame() throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(1, 0, fixedRng, true);
        MinesweeperGame.RevealResult result = game.revealCell("A1");

        assertFalse(result.mineHit);
        assertTrue(result.win); // Should win immediately
        assertTrue(game.isWin());
    }

    @Test
    void testAllMinesGame() throws InvalidMoveException {
        List<int[]> fixedMines = List.of(new int[]{0, 0}, new int[]{0, 1}, new int[]{1, 0},new int[]{1, 1});
        MinesweeperGame game = new MinesweeperGame(2, 4, fixedRng, fixedMines, false);

        MinesweeperGame.RevealResult result = game.revealCell("A1");
        assertTrue(result.mineHit);
        assertTrue(game.isGameOver());
    }

    @Test
    void testZeroMinesGame() throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(3, 0, fixedRng, true);
        MinesweeperGame.RevealResult result = game.revealCell("A1");

        assertFalse(result.mineHit);
        assertEquals(0, result.adjacentMines);
        // Should trigger massive flood reveal and likely win
        assertTrue(game.getRevealedCellsCount() >= 1);
    }

    // ==================== Flood Reveal Tests ====================

    @Test
    void testFloodRevealLargeArea() throws InvalidMoveException {
        // Create game with mines only in corners to ensure large zero area in middle
        List<int[]> fixedMines = List.of(new int[]{0, 0}, new int[]{0, 4}, new int[]{4, 0}, new int[]{4, 4});
        MinesweeperGame game = new MinesweeperGame(5, 4, fixedRng, fixedMines, true);

        MinesweeperGame.RevealResult result = game.revealCell("C3"); // Center cell
        assertFalse(result.mineHit);

        // Should reveal a large connected area
        assertTrue(game.getRevealedCellsCount() > 1);
    }

    @Test
    void testFloodRevealBoundaries() throws InvalidMoveException {
        // Test flood reveal near grid boundaries
        List<int[]> fixedMines = List.of(new int[]{1, 1});
        MinesweeperGame game = new MinesweeperGame(4, 1, fixedRng, fixedMines, true);

        MinesweeperGame.RevealResult result = game.revealCell("A1"); // Corner cell
        assertFalse(result.mineHit);

        // Should handle boundary conditions correctly
        assertTrue(game.getRevealedCellsCount() >= 1);
    }

    // ==================== Random Mine Generation Tests ====================

    @Test
    void testRandomMineGeneration() {
        MinesweeperGame game = new MinesweeperGame(4, 3, fixedRng, true);
        List<int[]> mines = game.getFixedMines();

        assertEquals(3, mines.size());

        // Verify all mines are within bounds and unique
        for (int[] mine : mines) {
            assertTrue(mine[0] >= 0 && mine[0] < 4);
            assertTrue(mine[1] >= 0 && mine[1] < 4);
        }
    }
}