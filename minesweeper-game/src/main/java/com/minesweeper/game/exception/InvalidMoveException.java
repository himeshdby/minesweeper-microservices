package com.minesweeper.game.exception;

/**
 * Thrown when user input is invalid.
 */
public class InvalidMoveException extends Exception {
    public InvalidMoveException(String message) { super(message); }
}