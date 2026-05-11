package sudoku.gui;

import sudoku.model.ISudokuModel;

// Handles GUI events and forwards valid requests to the model.
public class SudokuController {
    private static final int NO_SELECTION = -1;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 9;

    private final ISudokuModel model;
    private SudokuView view;
    private int selectedRow;
    private int selectedCol;

    /**
     * Creates a controller for the Sudoku model.
     *
     * @param model the model that stores game state and rules
     */
    public SudokuController(ISudokuModel model) {
        assert model != null : "model must not be null";
        this.model = model;
        this.selectedRow = NO_SELECTION;
        this.selectedCol = NO_SELECTION;
    }

    /**
     * Connects the view to this controller.
     *
     * @param view the GUI view
     */
    public void setView(SudokuView view) {
        assert view != null : "view must not be null";
        this.view = view;
        updateButtonStates();
    }

    /**
     * Handles a cell click.
     *
     * @param row the clicked row
     * @param col the clicked column
     */
    public void onCellClicked(int row, int col) {
        assert row >= 0 && row < MAX_NUMBER : "row must be in range 0-8";
        assert col >= 0 && col < MAX_NUMBER : "col must be in range 0-8";
        if (model.isEditable(row, col)) {
            selectedRow = row;
            selectedCol = col;
        } else {
            clearSelection();
        }
        updateButtonStates();
        refreshSelection();
    }

    /**
     * Handles number input from the user.
     *
     * @param number the input number from 1 to 9
     */
    public void onNumberInput(int number) {
        assert number >= MIN_NUMBER && number <= MAX_NUMBER : "number must be in range 1-9";
        if (!hasEditableSelection() || number < MIN_NUMBER || number > MAX_NUMBER) {
            return;
        }

        boolean changed = model.setValue(selectedRow, selectedCol, number);
        if (changed) {
            checkCompletion();
        }
        updateButtonStates();
    }

    /**
     * Handles the Erase button.
     */
    public void onEraseClicked() {
        if (!hasEditableSelection()) {
            return;
        }

        boolean changed = model.clearValue(selectedRow, selectedCol);
        updateButtonStates();
    }

    /**
     * Handles the Undo button.
     */
    public void onUndoClicked() {
        model.undo();
        updateButtonStates();
    }

    /**
     * Handles the Hint button.
     */
    public void onHintClicked() {
        if (!hasEditableSelection() || !model.isHintEnabled()) {
            return;
        }

        boolean changed = model.revealHint(selectedRow, selectedCol);
        if (changed) {
            checkCompletion();
        }
        updateButtonStates();
    }

    /**
     * Handles the Reset button.
     */
    public void onResetClicked() {
        model.reset();
        clearSelection();
        updateButtonStates();
    }

    /**
     * Handles the New Game button.
     */
    public void onNewGameClicked() {
        model.newGame();
        clearSelection();
        updateButtonStates();
    }

    /**
     * Handles the validation feedback checkbox.
     *
     * @param enabled true if validation feedback should be enabled
     */
    public void onValidationFeedbackChanged(boolean enabled) {
        model.setValidationFeedbackEnabled(enabled);
        updateButtonStates();
    }

    /**
     * Handles the hint enabled checkbox.
     *
     * @param enabled true if hints should be enabled
     */
    public void onHintEnabledChanged(boolean enabled) {
        model.setHintEnabled(enabled);
        updateButtonStates();
    }

    /**
     * Handles the random puzzle checkbox.
     *
     * @param enabled true if random puzzle selection should be enabled
     */
    public void onRandomPuzzleChanged(boolean enabled) {
        model.setRandomPuzzleSelectionEnabled(enabled);
        updateButtonStates();
    }

    /**
     * Handles keyboard movement between editable cells.
     *
     * @param rowChange the row movement
     * @param colChange the column movement
     */
    public void onSelectionMove(int rowChange, int colChange) {
        if (selectedRow == NO_SELECTION || selectedCol == NO_SELECTION) {
            selectFirstEditableCell();
            updateButtonStates();
            refreshSelection();
            return;
        }

        int startRow = selectedRow == NO_SELECTION ? 0 : selectedRow;
        int startCol = selectedCol == NO_SELECTION ? 0 : selectedCol;
        int row = startRow;
        int col = startCol;

        for (int step = 0; step < MAX_NUMBER * MAX_NUMBER; step++) {
            row = wrapCoordinate(row + rowChange);
            col = wrapCoordinate(col + colChange);
            if (model.isEditable(row, col)) {
                selectedRow = row;
                selectedCol = col;
                updateButtonStates();
                refreshSelection();
                return;
            }
        }
        clearSelection();
        updateButtonStates();
    }

    /**
     * Handles an input key that is not a valid Sudoku digit.
     *
     * @param input the rejected input text
     */
    public void onInvalidInput(String input) {
        if (view != null) {
            view.showInvalidInputMessage(input);
        }
    }

    /**
     * Returns whether a cell is currently selected.
     *
     * @param row the row to check
     * @param col the column to check
     * @return true if this cell is selected
     */
    public boolean isSelectedCell(int row, int col) {
        assert row >= 0 && row < MAX_NUMBER : "row must be in range 0-8";
        assert col >= 0 && col < MAX_NUMBER : "col must be in range 0-8";
        return selectedRow == row && selectedCol == col;
    }

    private boolean hasEditableSelection() {
        return selectedRow != NO_SELECTION
                && selectedCol != NO_SELECTION
                && model.isEditable(selectedRow, selectedCol);
    }

    private void clearSelection() {
        selectedRow = NO_SELECTION;
        selectedCol = NO_SELECTION;
    }

    private void updateButtonStates() {
        if (view == null) {
            return;
        }
        view.setEraseButtonEnabled(hasEditableSelection());
        view.setUndoButtonEnabled(model.canUndo());
        view.setHintButtonEnabled(model.isHintEnabled() && hasEditableSelection());
    }

    private void refreshSelection() {
        if (view != null) {
            view.refreshSelection();
        }
    }

    private void selectFirstEditableCell() {
        for (int row = 0; row < MAX_NUMBER; row++) {
            for (int col = 0; col < MAX_NUMBER; col++) {
                if (model.isEditable(row, col)) {
                    selectedRow = row;
                    selectedCol = col;
                    return;
                }
            }
        }
        clearSelection();
    }

    private int wrapCoordinate(int coordinate) {
        if (coordinate < 0) {
            return MAX_NUMBER - 1;
        }
        if (coordinate >= MAX_NUMBER) {
            return 0;
        }
        return coordinate;
    }

    private void checkCompletion() {
        if (view != null && model.isComplete()) {
            view.showCompletionMessage();
        }
    }
}
