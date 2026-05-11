package sudoku.model;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Observable;
import java.util.Objects;
import java.util.Random;

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

    /**
     * Creates a model and loads the first puzzle.
     *
     * @pre puzzleFile != null
     * @pre PuzzleLoader.loadAllPuzzles(puzzleFile).size() > 0
     *
     * @post invariant() == true
     * @post board.length == 9 && initialBoard.length == 9
     * @post currentPuzzleIndex == 0
     *
     * @param puzzleFile the path of the puzzles file
     */
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

    /**
     * Returns the value at one board position.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     *
     * @post 0 <= result <= 9
     * @post invariant() == true
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return the value at the cell, where 0 means empty
     */
    public int getValueAt(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before reading value";
        validateCoordinates(row, col);
        int result = board[row][col].getValue();
        assert isValueInRange(result) : "returned value must be in range 0-9";
        assert invariant() : "reading value must not change model state";
        return result;
    }

    /**
     * Returns whether a cell was part of the original puzzle.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     *
     * @post result == board[row][col].isPreFilled()
     * @post invariant() == true
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell is pre-filled
     */
    public boolean isPreFilled(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before reading pre-filled state";
        validateCoordinates(row, col);
        boolean result = board[row][col].isPreFilled();
        assert invariant() : "reading pre-filled state must not change model state";
        return result;
    }

    /**
     * Returns whether a cell can be edited by the user.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     *
     * @post result == !isPreFilled(row, col)
     * @post invariant() == true
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell is not pre-filled
     */
    public boolean isEditable(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before reading editable state";
        validateCoordinates(row, col);
        boolean result = !board[row][col].isPreFilled();
        assert invariant() : "reading editable state must not change model state";
        return result;
    }

    /**
     * Sets a value in an editable cell.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     * @pre 1 <= value <= 9
     * @pre isEditable(row, col) == true
     *
     * @post result == true ==> getValueAt(row, col) == value
     * @post result == true ==> canUndo() == true
     * @post result == false ==> board is unchanged
     * @post invariant() == true
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
        int oldValue = board[row][col].getValue();
        saveUndoMove(new Move(row, col, oldValue, value));
        board[row][col] = new Cell(value, false);
        notifyModelChanged();

        assert board[row][col].getValue() == value : "cell value was not updated";
        assert !board[row][col].isPreFilled() : "editable cell became pre-filled";
        assert invariant() : "model invariant failed after setting value";
        return true;
    }

    /**
     * Clears an editable cell.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     * @pre isEditable(row, col) == true
     *
     * @post result == true ==> getValueAt(row, col) == 0
     * @post result == true ==> canUndo() == true
     * @post result == false ==> board is unchanged
     * @post invariant() == true
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
        saveUndoMove(new Move(row, col, oldValue, EMPTY_VALUE));
        board[row][col] = new Cell(EMPTY_VALUE, false);
        notifyModelChanged();

        assert board[row][col].getValue() == EMPTY_VALUE : "cell was not cleared";
        assert !board[row][col].isPreFilled() : "editable cell became pre-filled";
        assert invariant() : "model invariant failed after clearing value";
        return true;
    }

    /**
     * Undoes the last user change.
     *
     * @pre true
     *
     * @post result == true ==> the latest editable cell change is reverted
     * @post result == false ==> board is unchanged
     * @post invariant() == true
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

        assert board[move.getRow()][move.getCol()].getValue() == move.getOldValue()
                : "undo did not restore old value";
        assert !board[move.getRow()][move.getCol()].isPreFilled() : "undo changed pre-filled state";
        assert invariant() : "model invariant failed after undo";
        return true;
    }

    /**
     * Returns whether there is one move that can be undone.
     *
     * @pre true
     *
     * @post result == !undoStack.isEmpty()
     * @post invariant() == true
     *
     * @return true if one move can be undone
     */
    public boolean canUndo() {
        assert invariant() : "model invariant failed before checking undo state";
        boolean result = !undoStack.isEmpty();
        assert invariant() : "checking undo state must not change model state";
        return result;
    }

    /**
     * Restores the board to the starting puzzle.
     *
     * @pre true
     *
     * @post forall row,col: board[row][col].equals(initialBoard[row][col])
     * @post undoStack.isEmpty() == true
     * @post invariant() == true
     */
    public void reset() {
        assert invariant() : "model invariant failed before reset";
        board = copyBoard(initialBoard);
        undoStack.clear();
        notifyModelChanged();
        assert boardsMatch(board, initialBoard) : "reset board does not match initial board";
        assert undoStack.isEmpty() : "undo stack should be empty after reset";
        assert invariant() : "model invariant failed after reset";
    }

    /**
     * Checks whether the puzzle is completely and correctly filled.
     *
     * @pre true
     *
     * @post result == (all cells are non-zero && isBoardValid())
     * @post invariant() == true
     *
     * @return true if the puzzle is complete
     */
    public boolean isComplete() {
        assert invariant() : "model invariant failed before completion check";
        boolean result = isFilled() && isBoardValid();
        assert invariant() : "completion check must not change model state";
        return result;
    }

    /**
     * Starts another puzzle.
     *
     * @pre allPuzzles != null && allPuzzles.size() > 0
     *
     * @post forall row,col: board[row][col].equals(initialBoard[row][col])
     * @post undoStack.isEmpty() == true
     * @post invariant() == true
     */
    public void newGame() {
        assert invariant() : "model invariant failed before new game";
        if (randomPuzzleSelectionEnabled) {
            currentPuzzleIndex = new Random().nextInt(allPuzzles.size());
        } else {
            currentPuzzleIndex = 0;
        }

        initializeBoard(PuzzleLoader.parsePuzzle(allPuzzles.get(currentPuzzleIndex)));
        undoStack.clear();
        notifyModelChanged();
        assert boardsMatch(board, initialBoard) : "new game board does not match initial board";
        assert undoStack.isEmpty() : "undo stack should be empty after new game";
        assert invariant() : "model invariant failed after new game";
    }

    /**
     * Fills one empty editable cell with a solved value.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     * @pre isEditable(row, col) == true
     * @pre getValueAt(row, col) == 0
     *
     * @post result == true ==> 1 <= getValueAt(row, col) <= 9
     * @post result == false ==> board is unchanged
     * @post invariant() == true
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if a hint was revealed
     */
    public boolean revealHint(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before hint";
        validateCoordinates(row, col);

        if (!hintEnabled || !isUserChangeAllowed(row, col) || board[row][col].getValue() != EMPTY_VALUE) {
            assert invariant() : "failed hint must not change model state";
            return false;
        }

        int solvedValue = solveCell(row, col);
        if (solvedValue == EMPTY_VALUE) {
            assert invariant() : "failed hint must not change model state";
            return false;
        }

        saveUndoMove(new Move(row, col, EMPTY_VALUE, solvedValue));
        board[row][col] = new Cell(solvedValue, false);
        notifyModelChanged();

        assert isInputValue(board[row][col].getValue()) : "hint must fill a value from 1 to 9";
        assert !board[row][col].isPreFilled() : "hint must not create a pre-filled cell";
        assert invariant() : "model invariant failed after hint";
        return true;
    }

    /**
     * Checks whether one cell follows row, column, and box rules.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     *
     * @post result == true ==> the value at row,col has no duplicate in its row, column, or box
     * @post invariant() == true
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell is valid
     */
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

    /**
     * Checks whether the whole board has no duplicate numbers.
     *
     * @pre true
     *
     * @post result == true ==> all rows, columns, and 3x3 boxes contain no duplicates except 0
     * @post invariant() == true
     *
     * @return true if all rows, columns, and boxes are valid
     */
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

    /**
     * Returns whether a cell should be shown as invalid.
     *
     * @pre 0 <= row < 9
     * @pre 0 <= col < 9
     *
     * @post result == (isValidationFeedbackEnabled() && !isCellValid(row, col))
     * @post invariant() == true
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if validation feedback is enabled and the cell is invalid
     */
    public boolean isCellInvalid(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";
        assert invariant() : "model invariant failed before checking invalid cell";
        validateCoordinates(row, col);

        boolean result = validationFeedbackEnabled && !isCellValid(row, col);
        assert invariant() : "validation must not change model state";
        return result;
    }

    /**
     * Returns whether invalid entries should be shown to the user.
     *
     * @pre true
     *
     * @post result == validationFeedbackEnabled
     * @post invariant() == true
     *
     * @return true if validation feedback is enabled
     */
    public boolean isValidationFeedbackEnabled() {
        assert invariant() : "model invariant failed before reading validation flag";
        boolean result = validationFeedbackEnabled;
        assert invariant() : "reading validation flag must not change model state";
        return result;
    }

    /**
     * Changes whether invalid entries should be shown to the user.
     *
     * @pre true
     *
     * @post validationFeedbackEnabled == enabled
     * @post invariant() == true
     *
     * @param enabled the new validation feedback setting
     */
    public void setValidationFeedbackEnabled(boolean enabled) {
        assert invariant() : "model invariant failed before changing validation flag";
        if (validationFeedbackEnabled != enabled) {
            validationFeedbackEnabled = enabled;
            notifyModelChanged();
        }
        assert validationFeedbackEnabled == enabled : "validation flag was not updated";
        assert invariant() : "model invariant failed after changing validation flag";
    }

    /**
     * Returns whether hints are enabled.
     *
     * @pre true
     *
     * @post result == hintEnabled
     * @post invariant() == true
     *
     * @return true if hints are enabled
     */
    public boolean isHintEnabled() {
        assert invariant() : "model invariant failed before reading hint flag";
        boolean result = hintEnabled;
        assert invariant() : "reading hint flag must not change model state";
        return result;
    }

    /**
     * Changes whether hints are enabled.
     *
     * @pre true
     *
     * @post hintEnabled == enabled
     * @post invariant() == true
     *
     * @param enabled the new hint setting
     */
    public void setHintEnabled(boolean enabled) {
        assert invariant() : "model invariant failed before changing hint flag";
        if (hintEnabled != enabled) {
            hintEnabled = enabled;
            notifyModelChanged();
        }
        assert hintEnabled == enabled : "hint flag was not updated";
        assert invariant() : "model invariant failed after changing hint flag";
    }

    /**
     * Returns whether new games should use random puzzle selection.
     *
     * @pre true
     *
     * @post result == randomPuzzleSelectionEnabled
     * @post invariant() == true
     *
     * @return true if random puzzle selection is enabled
     */
    public boolean isRandomPuzzleSelectionEnabled() {
        assert invariant() : "model invariant failed before reading random flag";
        boolean result = randomPuzzleSelectionEnabled;
        assert invariant() : "reading random flag must not change model state";
        return result;
    }

    /**
     * Changes whether new games should use random puzzle selection.
     *
     * @pre true
     *
     * @post randomPuzzleSelectionEnabled == enabled
     * @post invariant() == true
     *
     * @param enabled the new random puzzle setting
     */
    public void setRandomPuzzleSelectionEnabled(boolean enabled) {
        assert invariant() : "model invariant failed before changing random flag";
        if (randomPuzzleSelectionEnabled != enabled) {
            randomPuzzleSelectionEnabled = enabled;
            notifyModelChanged();
        }
        assert randomPuzzleSelectionEnabled == enabled : "random flag was not updated";
        assert invariant() : "model invariant failed after changing random flag";
    }

    /**
     * Checks the class invariants for this model.
     *
     * Class invariants:
     * board and initialBoard are 9x9, all cells are not null, all cell values are 0-9,
     * allPuzzles is not empty, undoStack is not null, and pre-filled values stay fixed.
     *
     * @pre true
     *
     * @post result == true iff all listed class invariants hold
     *
     * @return true if the model invariants hold
     */
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

    private void saveUndoMove(Move move) {
        assert move != null : "move must not be null";
        undoStack.clear();
        undoStack.push(move);
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

    private boolean isFilled() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col].getValue() == EMPTY_VALUE) {
                    return false;
                }
            }
        }
        return true;
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

    private boolean boardsMatch(Cell[][] firstBoard, Cell[][] secondBoard) {
        if (!hasValidBoardShape(firstBoard) || !hasValidBoardShape(secondBoard)) {
            return false;
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!firstBoard[row][col].equals(secondBoard[row][col])) {
                    return false;
                }
            }
        }
        return true;
    }

    private int solveCell(int row, int col) {
        assert isCoordinateInRange(row) : "row must be in range 0-8";
        assert isCoordinateInRange(col) : "col must be in range 0-8";

        int[][] solvedBoard = copyInitialValues();
        if (solveBoard(solvedBoard, 0)) {
            return solvedBoard[row][col];
        }
        return EMPTY_VALUE;
    }

    private int[][] copyInitialValues() {
        int[][] values = new int[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                values[row][col] = initialBoard[row][col].getValue();
            }
        }
        return values;
    }

    private boolean solveBoard(int[][] values, int cellIndex) {
        if (cellIndex == BOARD_SIZE * BOARD_SIZE) {
            return true;
        }

        int row = cellIndex / BOARD_SIZE;
        int col = cellIndex % BOARD_SIZE;
        if (values[row][col] != EMPTY_VALUE) {
            return solveBoard(values, cellIndex + 1);
        }

        for (int value = 1; value <= BOARD_SIZE; value++) {
            if (isValueAllowedInGrid(values, row, col, value)) {
                values[row][col] = value;
                if (solveBoard(values, cellIndex + 1)) {
                    return true;
                }
                values[row][col] = EMPTY_VALUE;
            }
        }
        return false;
    }

    private boolean isValueAllowedInGrid(int[][] values, int row, int col, int value) {
        return !valueExistsInGridRow(values, row, col, value)
                && !valueExistsInGridColumn(values, row, col, value)
                && !valueExistsInGridBox(values, row, col, value);
    }

    private boolean valueExistsInGridRow(int[][] values, int row, int ignoredCol, int value) {
        for (int col = 0; col < BOARD_SIZE; col++) {
            if (col != ignoredCol && values[row][col] == value) {
                return true;
            }
        }
        return false;
    }

    private boolean valueExistsInGridColumn(int[][] values, int ignoredRow, int col, int value) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (row != ignoredRow && values[row][col] == value) {
                return true;
            }
        }
        return false;
    }

    private boolean valueExistsInGridBox(int[][] values, int row, int col, int value) {
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int rowOffset = 0; rowOffset < 3; rowOffset++) {
            for (int colOffset = 0; colOffset < 3; colOffset++) {
                int checkedRow = startRow + rowOffset;
                int checkedCol = startCol + colOffset;
                if ((checkedRow != row || checkedCol != col) && values[checkedRow][checkedCol] == value) {
                    return true;
                }
            }
        }
        return false;
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
