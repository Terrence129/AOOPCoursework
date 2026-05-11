package sudoku.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sudoku.model.SudokuModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuModelTest {
    private static final int[][] COMPLETE_BOARD = {
            {1, 2, 3, 4, 5, 6, 7, 8, 9},
            {4, 5, 6, 7, 8, 9, 1, 2, 3},
            {7, 8, 9, 1, 2, 3, 4, 5, 6},
            {2, 3, 4, 5, 6, 7, 8, 9, 1},
            {5, 6, 7, 8, 9, 1, 2, 3, 4},
            {8, 9, 1, 2, 3, 4, 5, 6, 7},
            {3, 4, 5, 6, 7, 8, 9, 1, 2},
            {6, 7, 8, 9, 1, 2, 3, 4, 5},
            {9, 1, 2, 3, 4, 5, 6, 7, 8}
    };

    @TempDir
    Path tempDir;

    private SudokuModel model;

    @BeforeEach
    void setUp() throws IOException {
        Path puzzleFile = tempDir.resolve("puzzles.txt");
        Files.write(puzzleFile, Collections.singletonList(emptyPuzzleLine()), StandardCharsets.UTF_8);
        model = new SudokuModel(puzzleFile);
    }

    @Test
    void validationFeedbackShowsInvalidCellButDoesNotRejectTemporaryInvalidState() {
        // Scenario: validation feedback is on first, so a row duplicate is accepted but marked invalid.
        // Then validation feedback is off, so the duplicate remains but no invalid-cell feedback is shown.
        // The board is filled after that, but isComplete() must still be false because the row is invalid.
        assertTrue(model.setValue(0, 0, 1));
        assertTrue(model.isValidationFeedbackEnabled());
        assertTrue(model.setValue(0, 1, 1));
        assertEquals(1, model.getValueAt(0, 1));
        assertTrue(model.isCellInvalid(0, 0));
        assertTrue(model.isCellInvalid(0, 1));

        model.setValidationFeedbackEnabled(false);
        assertFalse(model.isCellInvalid(0, 0));
        assertFalse(model.isCellInvalid(0, 1));
        fillBoardExcept(0, 0, 0, 1);

        assertFalse(model.isBoardValid());
        assertFalse(model.isComplete());
    }

    @Test
    void undoRestoresOldValueAndResetKeepsOriginalPuzzleCells() {
        // Scenario: the user enters values in editable cells.
        // undo() should restore only the latest cell because the coursework requires single-level undo.
        // reset() should clear user input while keeping original clues.
        model = createModelWithPuzzle("100000000000000000000000000000000000000000000000000000000000000000000000000000000");

        assertTrue(model.isPreFilled(0, 0));
        assertEquals(1, model.getValueAt(0, 0));

        assertTrue(model.setValue(0, 1, 2));
        assertEquals(2, model.getValueAt(0, 1));
        assertFalse(model.setValue(0, 1, 2));
        assertTrue(model.setValue(0, 2, 3));
        assertEquals(3, model.getValueAt(0, 2));

        assertTrue(model.undo());
        assertEquals(0, model.getValueAt(0, 2));
        assertEquals(2, model.getValueAt(0, 1));
        assertFalse(model.undo());

        model.reset();

        assertEquals(0, model.getValueAt(0, 1));
        assertEquals(0, model.getValueAt(0, 2));
        assertEquals(1, model.getValueAt(0, 0));
        assertTrue(model.isPreFilled(0, 0));
    }

    @Test
    void completeValidBoardPassesCompletionButCompleteDuplicateBoardFails() {
        // Scenario: a correct full board should be complete.
        // After changing one editable cell to create a duplicate, the board is still full but no longer complete.
        setModelToBoard(COMPLETE_BOARD);
        assertTrue(model.isComplete());

        model.setValidationFeedbackEnabled(false);
        assertTrue(model.setValue(0, 0, COMPLETE_BOARD[0][1]));

        assertFalse(model.isBoardValid());
        assertFalse(model.isComplete());
    }

    private SudokuModel createModelWithPuzzle(String puzzleLine) {
        try {
            Path puzzleFile = tempDir.resolve("custom-puzzles.txt");
            Files.write(puzzleFile, Collections.singletonList(puzzleLine), StandardCharsets.UTF_8);
            return new SudokuModel(puzzleFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create test puzzle file.", exception);
        }
    }

    private void setModelToBoard(int[][] values) {
        for (int row = 0; row < values.length; row++) {
            for (int col = 0; col < values[row].length; col++) {
                assertTrue(model.setValue(row, col, values[row][col]));
            }
        }
    }

    private void fillBoardExcept(int skippedRowOne, int skippedColOne, int skippedRowTwo, int skippedColTwo) {
        for (int row = 0; row < COMPLETE_BOARD.length; row++) {
            for (int col = 0; col < COMPLETE_BOARD[row].length; col++) {
                if (!isSkippedCell(row, col, skippedRowOne, skippedColOne, skippedRowTwo, skippedColTwo)) {
                    assertTrue(model.setValue(row, col, COMPLETE_BOARD[row][col]));
                }
            }
        }
    }

    private boolean isSkippedCell(int row, int col, int skippedRowOne, int skippedColOne,
                                  int skippedRowTwo, int skippedColTwo) {
        return row == skippedRowOne && col == skippedColOne
                || row == skippedRowTwo && col == skippedColTwo;
    }

    private String emptyPuzzleLine() {
        StringBuilder puzzleLine = new StringBuilder();
        for (int index = 0; index < 81; index++) {
            puzzleLine.append('0');
        }
        return puzzleLine.toString();
    }
}
