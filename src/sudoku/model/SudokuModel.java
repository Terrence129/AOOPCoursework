package sudoku.model;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Observable;
import java.util.Objects;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/11 16:23
 */
// Core model class that stores Sudoku state and basic model settings.
public class SudokuModel extends Observable {
    private static final int BOARD_SIZE = 9;
    private static final int EMPTY_VALUE = 0;

    private Cell[][] board;
    private Cell[][] initialBoard;
    private Deque<Move> undoStack;
    private List<String> allPuzzles;
    private int currentPuzzleIndex;
    private boolean validationFeedbackEnabled;
    private boolean hintEnabled;
    private boolean randomPuzzleSelectionEnabled;

    // Loads puzzle data and starts with the first puzzle.
    public SudokuModel(Path puzzleFile) {
        assert puzzleFile != null : "puzzleFile must not be null";

        Objects.requireNonNull(puzzleFile, "puzzleFile must not be null");
        allPuzzles = new ArrayList<String>(PuzzleLoader.loadAllPuzzles(puzzleFile));
        if (allPuzzles.isEmpty()) {
            throw new IllegalArgumentException("Puzzle file must contain at least one puzzle.");
        }

        undoStack = new ArrayDeque<Move>();
        currentPuzzleIndex = 0;
        validationFeedbackEnabled = true;
        hintEnabled = true;
        randomPuzzleSelectionEnabled = false;
        initializeBoard(PuzzleLoader.parsePuzzle(allPuzzles.get(currentPuzzleIndex)));
        notifyModelChanged();

        assert invariant() : "model invariant failed after construction";
    }

    // Returns the value at one board position.
    public int getValueAt(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        validateCoordinates(row, col);
        return board[row][col].getValue();
    }

    // Returns true if the cell was part of the original puzzle.
    public boolean isPreFilled(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        validateCoordinates(row, col);
        return board[row][col].isPreFilled();
    }

    // Returns true if the user is allowed to change this cell.
    public boolean isEditable(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        validateCoordinates(row, col);
        return !board[row][col].isPreFilled();
    }

    // Returns whether invalid entries should be shown to the user.
    public boolean isValidationFeedbackEnabled() {
        assert invariant() : "model invariant failed before reading validation flag";
        return validationFeedbackEnabled;
    }

    // Changes whether invalid entries should be shown to the user.
    public void setValidationFeedbackEnabled(boolean enabled) {
        assert invariant() : "model invariant failed before changing validation flag";
        if (validationFeedbackEnabled != enabled) {
            validationFeedbackEnabled = enabled;
            notifyModelChanged();
        }
        assert invariant() : "model invariant failed after changing validation flag";
    }

    // Returns whether hints are enabled.
    public boolean isHintEnabled() {
        assert invariant() : "model invariant failed before reading hint flag";
        return hintEnabled;
    }

    // Changes whether hints are enabled.
    public void setHintEnabled(boolean enabled) {
        assert invariant() : "model invariant failed before changing hint flag";
        if (hintEnabled != enabled) {
            hintEnabled = enabled;
            notifyModelChanged();
        }
        assert invariant() : "model invariant failed after changing hint flag";
    }

    // Returns whether new games should use random puzzle selection.
    public boolean isRandomPuzzleSelectionEnabled() {
        assert invariant() : "model invariant failed before reading random flag";
        return randomPuzzleSelectionEnabled;
    }

    // Changes whether new games should use random puzzle selection.
    public void setRandomPuzzleSelectionEnabled(boolean enabled) {
        assert invariant() : "model invariant failed before changing random flag";
        if (randomPuzzleSelectionEnabled != enabled) {
            randomPuzzleSelectionEnabled = enabled;
            notifyModelChanged();
        }
        assert invariant() : "model invariant failed after changing random flag";
    }

    // Checks the main rules that should always be true inside the model.
    public boolean invariant() {
        return hasValidBoardShape(board)
                && hasValidBoardShape(initialBoard)
                && undoStack != null
                && allPuzzles != null
                && !allPuzzles.isEmpty()
                && currentPuzzleIndex >= 0
                && currentPuzzleIndex < allPuzzles.size()
                && hasValidCells(board)
                && hasValidCells(initialBoard)
                && originalCellsAreStillFixed()
                && undoStackContainsOnlyEditableCells();
    }

    private void initializeBoard(int[][] puzzleData) {
        assert puzzleData != null : "puzzleData must not be null";
        assert puzzleData.length == BOARD_SIZE : "puzzleData must have 9 rows";

        board = new Cell[BOARD_SIZE][BOARD_SIZE];
        initialBoard = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            assert puzzleData[row].length == BOARD_SIZE : "puzzleData row must have 9 columns";
            for (int col = 0; col < BOARD_SIZE; col++) {
                int value = puzzleData[row][col];
                boolean preFilled = value != EMPTY_VALUE;
                board[row][col] = new Cell(value, preFilled);
                initialBoard[row][col] = new Cell(value, preFilled);
            }
        }
    }

    private void validateCoordinates(int row, int col) {
        if (!isCoordinateInRange(row) || !isCoordinateInRange(col)) {
            throw new IllegalArgumentException("Row and column must be between 0 and 8.");
        }
    }

    private boolean isCoordinateInRange(int coordinate) {
        return coordinate >= 0 && coordinate < BOARD_SIZE;
    }

    private void notifyModelChanged() {
        setChanged();
        notifyObservers();
    }

    private boolean hasValidBoardShape(Cell[][] cells) {
        if (cells == null || cells.length != BOARD_SIZE) {
            return false;
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (cells[row] == null || cells[row].length != BOARD_SIZE) {
                return false;
            }
        }
        return true;
    }

    private boolean hasValidCells(Cell[][] cells) {
        if (!hasValidBoardShape(cells)) {
            return false;
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell cell = cells[row][col];
                if (cell == null || !isValueInRange(cell.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean originalCellsAreStillFixed() {
        if (!hasValidBoardShape(board) || !hasValidBoardShape(initialBoard)) {
            return false;
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell originalCell = initialBoard[row][col];
                Cell currentCell = board[row][col];
                if (originalCell.isPreFilled() && currentCell.getValue() != originalCell.getValue()) {
                    return false;
                }
                if (originalCell.isPreFilled() != currentCell.isPreFilled()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean undoStackContainsOnlyEditableCells() {
        for (Move move : undoStack) {
            if (move == null
                    || !isCoordinateInRange(move.getRow())
                    || !isCoordinateInRange(move.getCol())
                    || initialBoard[move.getRow()][move.getCol()].isPreFilled()
                    || !isValueInRange(move.getOldValue())
                    || !isValueInRange(move.getNewValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean isValueInRange(int value) {
        return value >= EMPTY_VALUE && value <= BOARD_SIZE;
    }
}
