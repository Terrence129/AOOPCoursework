package sudoku.gui;

import sudoku.model.SudokuModel;

// Handles GUI events and forwards valid requests to the model.
public class SudokuController {
    private static final int NO_SELECTION = -1;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 9;

    private SudokuModel model;
    private SudokuView view;
    private int selectedRow;
    private int selectedCol;
    private int undoMoveCount;

    /**
     * Creates a controller for the Sudoku model.
     *
     * @param model the model that stores game state and rules
     */
    public SudokuController(SudokuModel model) {
        assert model != null : "model must not be null";
        this.model = model;
        this.selectedRow = NO_SELECTION;
        this.selectedCol = NO_SELECTION;
        this.undoMoveCount = 0;
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
            undoMoveCount++;
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
        if (changed) {
            undoMoveCount++;
        }
        updateButtonStates();
    }

    /**
     * Handles the Undo button.
     */
    public void onUndoClicked() {
        boolean changed = model.undo();
        if (changed && undoMoveCount > 0) {
            undoMoveCount--;
        }
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
            undoMoveCount++;
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
        undoMoveCount = 0;
        updateButtonStates();
    }

    /**
     * Handles the New Game button.
     */
    public void onNewGameClicked() {
        model.newGame();
        clearSelection();
        undoMoveCount = 0;
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
        view.setUndoButtonEnabled(undoMoveCount > 0);
        view.setHintButtonEnabled(model.isHintEnabled() && hasEditableSelection());
    }

    private void checkCompletion() {
        if (view != null && model.isComplete()) {
            view.showCompletionMessage();
        }
    }
}
