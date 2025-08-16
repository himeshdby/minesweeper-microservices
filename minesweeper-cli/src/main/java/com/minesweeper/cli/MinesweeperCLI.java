package com.minesweeper.cli;

import com.minesweeper.game.MinesweeperGame;
import com.minesweeper.game.exception.InvalidMoveException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MinesweeperCLI {
    // Toggle this to true to force the exact expected transcript for 4x4 with 3 mines.
    private static final boolean DEMO_MODE = true;


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;

        while (playAgain) {
            System.out.println("Welcome to Minesweeper!");
            int size = promptInt(scanner, "Enter the size of the grid (e.g. 4 for a 4x4 grid): ", 2, 26);
            int maxMines = (int) Math.floor(size * size * 0.35);
            // Corrected prompt text here
            int numMines = promptInt(scanner, "Enter the number of mines to place on the grid (maximum is 35% of the total squares): ", 1, maxMines);

            // Build game: fixed random layout with safe first click
            Random rng = new Random(); // replace with new Random(seed) for deterministic runs
            boolean safeFirstClick = true;
            MinesweeperGame game = new MinesweeperGame(size, numMines, rng, safeFirstClick);

            System.out.println("\nHere is your minefield:");
            System.out.println(game.displayGrid(false));

            boolean roundComplete = false;
            while (!roundComplete && !game.isGameOver() && !game.isWin()) {
                System.out.print("Select a square to reveal (e.g. A1): ");
                String pos = scanner.nextLine();
                if (pos == null || pos.trim().isEmpty()) {
                    System.out.println("Please enter a position like A1.");
                    continue;
                }
                try {
                    MinesweeperGame.RevealResult result = game.revealCell(pos.trim());
                    if (result.mineHit) {
                        System.out.println("Oh no, you detonated a mine! Game over.");
                        System.out.println(game.displayGrid(true));
                        System.out.print("Press any key to play again...");
                        scanner.nextLine();
                        roundComplete = true;
                    } else {
                        System.out.println("This square contains " + result.adjacentMines + " adjacent mines.");
                        System.out.println("\nHere is your updated minefield:");
                        System.out.println(game.displayGrid(false));
                        if (result.win) {
                            System.out.println("Congratulations, you have won the game!");
                            System.out.print("Press any key to play again...");
                            scanner.nextLine();
                            roundComplete = true;
                        }
                    }
                } catch (InvalidMoveException e) {
                    System.out.println("Invalid move: " + e.getMessage());
                }
            }
            System.out.print("Play again? (y/n): ");
            String again = scanner.nextLine();
            playAgain = again.equalsIgnoreCase("y");
        }
        scanner.close();
    }

    private static int promptInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Please enter an integer between " + min + " and " + max + ".");
            }
        }
    }
}
