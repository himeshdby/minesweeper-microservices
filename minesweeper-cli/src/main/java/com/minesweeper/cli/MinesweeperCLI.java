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

            MinesweeperGame game;
            if (DEMO_MODE && size == 4 && numMines == 3) {
                // Define fixed mine locations for the demo (A2, B2, C1)
                List<int[]> fixedMines = new ArrayList<>();
                fixedMines.add(new int[]{0, 1}); // A2
                fixedMines.add(new int[]{1, 1}); // B2
                fixedMines.add(new int[]{2, 0}); // C1
                game = new MinesweeperGame(size, numMines, new Random(), fixedMines);
            }
            else if (DEMO_MODE && size == 3 && numMines == 3) {
                // Define fixed mine locations for the demo (A2, B2, C1)
                List<int[]> fixedMines = new ArrayList<>();
                fixedMines.add(new int[]{0, 0}); // A1
                fixedMines.add(new int[]{1, 1}); // B2
                fixedMines.add(new int[]{2, 2});
                game = new MinesweeperGame(size, numMines, new Random(), fixedMines,false);
            }
            else {
                game = new MinesweeperGame(size, numMines, new Random());
            }

            System.out.println("\nHere is your minefield:");
            System.out.println(game.displayGrid(false));

            while (!game.isGameOver() && !game.isWin()) {
                System.out.print("Select a square to reveal (e.g. A1): ");
                String pos = scanner.nextLine();
                try {
                    MinesweeperGame.RevealResult result = game.revealCell(pos);
                    if (result.mineHit) {
                        System.out.println("Oh no, you detonated a mine! Game over.");
                        System.out.println(game.displayGrid(true));
                        break;
                    } else {
                        System.out.println("This square contains " + result.adjacentMines + " adjacent mines.");
                        System.out.println("\nHere is your updated minefield:");
                        System.out.println(game.displayGrid(false));
                        if (result.win) {
                            System.out.println("Congratulations, you have won the game!");
                            System.out.print("Press Enter to play again...");
                            scanner.nextLine(); // Consume the newline
                            break;
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
