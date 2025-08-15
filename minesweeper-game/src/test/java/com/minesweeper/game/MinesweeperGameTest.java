package com.minesweeper.game;

import com.minesweeper.game.exception.InvalidMoveException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MinesweeperGameTest {

    // Helper to set private fields


    // Helper to get private fields
    private static Object getField(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    @Test
    void testInvalidMove() {
        MinesweeperGame game = new MinesweeperGame(3, 1);
        assertThrows(InvalidMoveException.class, () -> game.revealCell("Z9"));
    }

    @Test
    void testAlreadyRevealedCell() throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(3, 1);
        game.revealCell("A1"); // Reveal A1 once
        assertThrows(InvalidMoveException.class, () -> game.revealCell("A1")); // Try to reveal again
    }




    @Test
    void testDisplayGrid_ShowAllAndHidden() {
        MinesweeperGame game = new MinesweeperGame(2, 1, new Random(), null);
        Grid g = (Grid) getField(game, "grid");

        Cell c = g.getCell(0, 0);
        c.setMine(true);
        c.setAdjacentMines(3);

        String showAll = game.displayGrid(true);
        assertTrue(showAll.contains("*") || showAll.contains("3"));

        String hidden = game.displayGrid(false);
        assertTrue(hidden.contains("_"));
    }

    @Test
    void testRevealCell_GameOverThrows() {
        MinesweeperGame game = new MinesweeperGame(2, 1, new Random(), null);
        setField(game, "gameOver", true);
        assertThrows(InvalidMoveException.class, () -> game.revealCell("A1"));
    }

    @Test
    void testRevealCell_InvalidPositions() {
        MinesweeperGame game = new MinesweeperGame(2, 1, new Random(), null);
        assertThrows(InvalidMoveException.class, () -> game.revealCell(null));
        assertThrows(InvalidMoveException.class, () -> game.revealCell("A"));
        assertThrows(InvalidMoveException.class, () -> game.revealCell("11"));
        assertThrows(InvalidMoveException.class, () -> game.revealCell("Axx"));
        assertThrows(InvalidMoveException.class, () -> game.revealCell("Z9"));
    }

    @Test
    void testRevealCell_FirstClickWithFixedMinesRelocation() throws InvalidMoveException {
        List<int[]> fixedMines = new ArrayList<>();
        fixedMines.add(new int[]{0, 0}); // first click mine

        MinesweeperGame game = new MinesweeperGame(3, 1, new Random(), fixedMines);
        Grid g = (Grid) getField(game, "grid");
        g.getCell(0, 0).setMine(false);
        g.getCell(0, 0).setAdjacentMines(1);

        MinesweeperGame.RevealResult result = game.revealCell("A1");
        assertFalse(result.mineHit);
    }

    @Test
    void testRevealCell_MineHit() throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(2, 1, new Random(), null);
        Grid g = (Grid) getField(game, "grid");

        g.placeMinesFirstClick(0, 0);
        g.getCell(0, 0).setMine(true);

        MinesweeperGame.RevealResult result = game.revealCell("A1");
        assertTrue(result.mineHit);
        assertTrue(game.isGameOver());
    }

    @Test
    void testRevealCell_AlreadyRevealedThrows() throws InvalidMoveException {
        MinesweeperGame game = new MinesweeperGame(2, 1, new Random(), null);
        Grid g = (Grid) getField(game, "grid");

        g.placeMinesFirstClick(0, 0);
        Cell cell = g.getCell(0, 0);
        cell.setMine(false);
        cell.setRevealed(true);

        assertThrows(InvalidMoveException.class, () -> game.revealCell("A1"));
    }


    // Helper to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = MinesweeperGame.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
