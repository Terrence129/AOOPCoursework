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

    /**
     * Sets a value in an editable cell.
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @param value the new value from 1 to 9
     * @return true if the value was set
     */
    public boolean setValue(int row, int col, int value) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert value >= 1 && value <= BOARD_SIZE : "value must be in range 1-9";
        assert invariant() : "model invariant failed before setting value";
        validateCoordinates(row, col);

        if (!isUserChangeAllowed(row, col) || !isInputValue(value)) {
            assert invariant() : "failed set must not change model state";
            return false;
        }
        if (validationFeedbackEnabled && !isValueValidAt(row, col, value)) {
            assert invariant() : "failed set must not change model state";
            return false;
        }

        int oldValue = board[row][col].getValue();
        undoStack.push(new Move(row, col, oldValue, value));
        board[row][col] = new Cell(value, false);
        notifyModelChanged();

        assert invariant() : "model invariant failed after setting value";
        return true;
    }

    /**
     * Clears an editable cell.
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell was cleared
     */
    public boolean clearValue(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before clearing value";
        validateCoordinates(row, col);

        if (!isUserChangeAllowed(row, col)) {
            assert invariant() : "failed clear must not change model state";
            return false;
        }

        int oldValue = board[row][col].getValue();
        undoStack.push(new Move(row, col, oldValue, EMPTY_VALUE));
        board[row][col] = new Cell(EMPTY_VALUE, false);
        notifyModelChanged();

        assert invariant() : "model invariant failed after clearing value";
        return true;
    }

    /**
     * Undoes the last user change.
     *
     * @return true if a move was undone
     */
    public boolean undo() {
        assert invariant() : "model invariant failed before undo";
        if (undoStack.isEmpty()) {
            assert invariant() : "failed undo must not change model state";
            return false;
        }

        Move move = undoStack.pop();
        if (!isUserChangeAllowed(move.getRow(), move.getCol())) {
            assert invariant() : "model invariant failed after blocked undo";
            return false;
        }

        board[move.getRow()][move.getCol()] = new Cell(move.getOldValue(), false);
        notifyModelChanged();

        assert invariant() : "model invariant failed after undo";
        return true;
    }

    /**
     * Restores the board to the starting puzzle.
     */
    public void reset() {
        assert invariant() : "model invariant failed before reset";
        board = copyBoard(initialBoard);
        undoStack.clear();
        notifyModelChanged();
        assert invariant() : "model invariant failed after reset";
    }

    // Checks whether one cell follows row, column, and box rules.
    public boolean isCellValid(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before checking cell";
        validateCoordinates(row, col);

        int value = board[row][col].getValue();
        boolean result;
        if (value == EMPTY_VALUE) {
            result = true;
        } else {
            result = countValueInRow(row, value) == 1
                    && countValueInColumn(col, value) == 1
                    && countValueInBox(row, col, value) == 1;
        }

        assert invariant() : "validation must not change model state";
        return result;
    }

    // Checks whether the whole board has no duplicate numbers.
    public boolean isBoardValid() {
        assert invariant() : "model invariant failed before checking board";
        boolean result = true;
        for (int index = 0; index < BOARD_SIZE; index++) {
            if (!isRowValid(index) || !isColumnValid(index)) {
                result = false;
                break;
            }
        }

        if (result) {
            for (int row = 0; row < BOARD_SIZE; row += 3) {
                for (int col = 0; col < BOARD_SIZE; col += 3) {
                    if (!isBoxValid(row, col)) {
                        result = false;
                    }
                }
            }
        }
        assert invariant() : "validation must not change model state";
        return result;
    }

    // Returns true when a cell should be shown as invalid.
    public boolean isCellInvalid(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before checking invalid cell";
        validateCoordinates(row, col);

        boolean result = validationFeedbackEnabled && !isCellValid(row, col);
        assert invariant() : "validation must not change model state";
        return result;
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

    private boolean isRowValid(int row) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        return !hasDuplicateInRow(row);
    }

    private boolean isColumnValid(int col) {
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        return !hasDuplicateInColumn(col);
    }

    private boolean isBoxValid(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        return !hasDuplicateInBox(row, col);
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

    private boolean isInputValue(int value) {
        return value >= 1 && value <= BOARD_SIZE;
    }

    private boolean isUserChangeAllowed(int row, int col) {
        return isCoordinateInRange(row)
                && isCoordinateInRange(col)
                && !initialBoard[row][col].isPreFilled();
    }

    private Cell[][] copyBoard(Cell[][] source) {
        Cell[][] copy = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell cell = source[row][col];
                copy[row][col] = new Cell(cell.getValue(), cell.isPreFilled());
            }
        }
        return copy;
    }

    private boolean isValueValidAt(int row, int col, int value) {
        return !valueExistsInRow(row, col, value)
                && !valueExistsInColumn(row, col, value)
                && !valueExistsInBox(row, col, value);
    }

    private boolean valueExistsInRow(int row, int ignoredCol, int value) {
        for (int col = 0; col < BOARD_SIZE; col++) {
            if (col != ignoredCol && board[row][col].getValue() == value) {
                return true;
            }
        }
        return false;
    }

    private boolean valueExistsInColumn(int ignoredRow, int col, int value) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (row != ignoredRow && board[row][col].getValue() == value) {
                return true;
            }
        }
        return false;
    }

    private boolean valueExistsInBox(int row, int col, int value) {
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int rowOffset = 0; rowOffset < 3; rowOffset++) {
            for (int colOffset = 0; colOffset < 3; colOffset++) {
                int checkedRow = startRow + rowOffset;
                int checkedCol = startCol + colOffset;
                if ((checkedRow != row || checkedCol != col)
                        && board[checkedRow][checkedCol].getValue() == value) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDuplicateInRow(int row) {
        boolean[] seen = new boolean[BOARD_SIZE + 1];
        for (int col = 0; col < BOARD_SIZE; col++) {
            int value = board[row][col].getValue();
            if (isDuplicateValue(value, seen)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDuplicateInColumn(int col) {
        boolean[] seen = new boolean[BOARD_SIZE + 1];
        for (int row = 0; row < BOARD_SIZE; row++) {
            int value = board[row][col].getValue();
            if (isDuplicateValue(value, seen)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDuplicateInBox(int row, int col) {
        boolean[] seen = new boolean[BOARD_SIZE + 1];
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int rowOffset = 0; rowOffset < 3; rowOffset++) {
            for (int colOffset = 0; colOffset < 3; colOffset++) {
                int value = board[startRow + rowOffset][startCol + colOffset].getValue();
                if (isDuplicateValue(value, seen)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDuplicateValue(int value, boolean[] seen) {
        if (value == EMPTY_VALUE) {
            return false;
        }
        if (seen[value]) {
            return true;
        }
        seen[value] = true;
        return false;
    }

    private int countValueInRow(int row, int value) {
        int count = 0;
        for (int col = 0; col < BOARD_SIZE; col++) {
            if (board[row][col].getValue() == value) {
                count++;
            }
        }
        return count;
    }

    private int countValueInColumn(int col, int value) {
        int count = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (board[row][col].getValue() == value) {
                count++;
            }
        }
        return count;
    }

    private int countValueInBox(int row, int col, int value) {
        int count = 0;
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int rowOffset = 0; rowOffset < 3; rowOffset++) {
            for (int colOffset = 0; colOffset < 3; colOffset++) {
                if (board[startRow + rowOffset][startCol + colOffset].getValue() == value) {
                    count++;
                }
            }
        }
        return count;
    }
}
