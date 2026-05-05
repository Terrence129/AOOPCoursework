package sudoku.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/11 16:09
 */
// Loads and parses Sudoku puzzle data for the model layer.
public final class PuzzleLoader {
    private static final int BOARD_SIZE = 9;
    private static final int PUZZLE_LENGTH = BOARD_SIZE * BOARD_SIZE;

    private PuzzleLoader() {
        // This class only has static helper methods.
    }

    /**
     * Loads all non-empty puzzle lines from a text file.
     *
     * @param filePath the path to the puzzles file
     * @return all puzzle lines found in the file
     * @throws IllegalArgumentException if the file cannot be read
     */
    public static List<String> loadAllPuzzles(Path filePath) {
        assert filePath != null : "filePath must not be null";

        List<String> puzzles = new ArrayList<String>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line = reader.readLine();
            while (line != null) {
                String puzzleLine = line.trim();
                if (!puzzleLine.isEmpty()) {
                    puzzles.add(puzzleLine);
                }
                line = reader.readLine();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read puzzle file: " + filePath, exception);
        }

        assert puzzles != null : "puzzles must not be null";
        return puzzles;
    }

    /**
     * Parses one 81-character puzzle line into a 9x9 board.
     *
     * @param puzzleLine the puzzle line, using 0 for empty cells
     * @return a 9x9 integer board
     * @throws IllegalArgumentException if the line is not 81 digits
     */
    public static int[][] parsePuzzle(String puzzleLine) {
        assert puzzleLine != null : "puzzleLine must not be null";
        validatePuzzleLine(puzzleLine);

        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
        for (int index = 0; index < PUZZLE_LENGTH; index++) {
            int row = index / BOARD_SIZE;
            int col = index % BOARD_SIZE;
            board[row][col] = Character.getNumericValue(puzzleLine.charAt(index));
        }

        assert board.length == BOARD_SIZE : "board must have 9 rows";
        assert board[0].length == BOARD_SIZE : "board must have 9 columns";
        return board;
    }

    private static void validatePuzzleLine(String puzzleLine) {
        if (puzzleLine.length() != PUZZLE_LENGTH) {
            throw new IllegalArgumentException("Puzzle line must contain exactly 81 characters.");
        }

        for (int index = 0; index < puzzleLine.length(); index++) {
            if (!Character.isDigit(puzzleLine.charAt(index))) {
                throw new IllegalArgumentException("Puzzle line must contain only digits.");
            }
        }
    }
}
