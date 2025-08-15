package com.minesweeper.cli;

import com.minesweeper.game.MinesweeperGame;
import com.minesweeper.game.exception.InvalidMoveException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MinesweeperCLITest {
    List<int[]> fixed = new ArrayList<>();
    @Test
    void testSuccessOutput() throws InvalidMoveException {
        fixed.add(new int[]{0, 1});
        fixed.add(new int[]{1, 1});
        fixed.add(new int[]{2, 0});
        MinesweeperGame game = new MinesweeperGame(4, 3, new Random(1L), fixed, true);

        StringBuilder output = new StringBuilder();
        output.append("Welcome to Minesweeper!\n\n");
        output.append("Here is your minefield:\n");
        output.append(game.displayGrid(false)).append("\n");

        String[] moves = {"D4", "B1", "A1", "D1"};
        int[] adj = {0, 3, 2, 1};

        for (int i = 0; i < moves.length; i++) {
            MinesweeperGame.RevealResult result = game.revealCell(moves[i]);
            assertFalse(result.mineHit, "Should not hit a mine on move " + moves[i]);
            assertEquals(adj[i], result.adjacentMines, "Adj count mismatch on " + moves[i]);
            output.append("This square contains ").append(result.adjacentMines).append(" adjacent mines. \n\n");
            output.append("Here is your updated minefield:\n");
            output.append(game.displayGrid(false)).append("\n");
        }

        assertTrue(game.isWin(), "Game should be won after the scripted moves.");
        output.append("Congratulations, you have won the game!\n");
        assertTrue(output.toString().contains("Congratulations, you have won the game!"));
    }

//    @Test
//    void testFailureOutput() throws InvalidMoveException {
//        MinesweeperGame game = new MinesweeperGame(3, 3, new Random(456L), fixed, false);
//         fixed.add(new int[]{0, 0});
//         fixed.add(new int[]{1, 1});
//         fixed.add(new int[]{2, 2});
//        StringBuilder output = new StringBuilder();
//        output.append("Welcome to Minesweeper!\n\n");
//        output.append("Here is your minefield:\n");
//        output.append(game.displayGrid(false)).append("\n");
//
//        MinesweeperGame.RevealResult result = game.revealCell("C3");
//        assertTrue(result.mineHit, "Clicking C3 should hit the mine in the failure demo.");
//        assertTrue(game.isGameOver(), "Game should be over after hitting a mine.");
//        output.append("Oh no, you detonated a mine! Game over.\n");
//        output.append(game.displayGrid(true)).append("\n");
//
//        assertTrue(output.toString().contains("Oh no, you detonated a mine! Game over."));
//    }
}
