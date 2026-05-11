package sudoku.model;

import java.util.Observer;

/**
 * Defines the public operations provided by the Sudoku model.
 *
 * @invariant the board is 9x9 and all public operations preserve the model state rules
 */
public interface ISudokuModel {
    /**
     * Returns the value at one board position.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @ensures 0 <= result <= 9
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return the value at the cell, where 0 means empty
     */
    int getValueAt(int row, int col);

    /**
     * Returns whether a cell was part of the original puzzle.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @ensures result tells whether the cell is pre-filled
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell is pre-filled
     */
    boolean isPreFilled(int row, int col);

    /**
     * Returns whether a cell can be edited by the user.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @ensures result == !isPreFilled(row, col)
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell is not pre-filled
     */
    boolean isEditable(int row, int col);

    /**
     * Sets a value in an editable cell.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @requires 1 <= value <= 9
     * @ensures result is true only when the board is changed
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @param value the new value from 1 to 9
     * @return true if the value was set
     */
    boolean setValue(int row, int col, int value);

    /**
     * Clears an editable cell.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @ensures result is true only when the board is changed
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell was cleared
     */
    boolean clearValue(int row, int col);

    /**
     * Undoes the latest user change.
     *
     * @requires true
     * @ensures result is true only when one move is undone
     *
     * @return true if a move was undone
     */
    boolean undo();

    /**
     * Returns whether a move can be undone.
     *
     * @requires true
     * @ensures result tells whether the single undo slot is filled
     *
     * @return true if one move can be undone
     */
    boolean canUndo();

    /**
     * Restores the board to the starting puzzle.
     *
     * @requires true
     * @ensures the board matches the initial puzzle
     */
    void reset();

    /**
     * Checks whether the puzzle is completely and correctly filled.
     *
     * @requires true
     * @ensures result is true only when the board is full and valid
     *
     * @return true if the puzzle is complete
     */
    boolean isComplete();

    /**
     * Starts a new puzzle.
     *
     * @requires at least one puzzle has been loaded
     * @ensures the board is reset to the selected puzzle
     */
    void newGame();

    /**
     * Reveals a hint in one empty editable cell.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @ensures result is true only when a hint is written
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if a hint was revealed
     */
    boolean revealHint(int row, int col);

    /**
     * Checks whether one cell follows the Sudoku rules.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @ensures the board is not changed
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell is valid
     */
    boolean isCellValid(int row, int col);

    /**
     * Checks whether the whole board follows the Sudoku rules.
     *
     * @requires true
     * @ensures the board is not changed
     *
     * @return true if all rows, columns, and boxes are valid
     */
    boolean isBoardValid();

    /**
     * Returns whether a cell should be shown as invalid.
     *
     * @requires 0 <= row < 9
     * @requires 0 <= col < 9
     * @ensures result depends on the validation feedback flag
     *
     * @param row the row using the internal 0-8 coordinate system
     * @param col the column using the internal 0-8 coordinate system
     * @return true if the cell should be shown as invalid
     */
    boolean isCellInvalid(int row, int col);

    /**
     * Returns whether validation feedback is enabled.
     *
     * @requires true
     * @ensures the model is not changed
     *
     * @return true if validation feedback is enabled
     */
    boolean isValidationFeedbackEnabled();

    /**
     * Changes whether validation feedback is enabled.
     *
     * @requires true
     * @ensures isValidationFeedbackEnabled() == enabled
     *
     * @param enabled the new validation feedback setting
     */
    void setValidationFeedbackEnabled(boolean enabled);

    /**
     * Returns whether hints are enabled.
     *
     * @requires true
     * @ensures the model is not changed
     *
     * @return true if hints are enabled
     */
    boolean isHintEnabled();

    /**
     * Changes whether hints are enabled.
     *
     * @requires true
     * @ensures isHintEnabled() == enabled
     *
     * @param enabled the new hint setting
     */
    void setHintEnabled(boolean enabled);

    /**
     * Returns whether random puzzle selection is enabled.
     *
     * @requires true
     * @ensures the model is not changed
     *
     * @return true if random puzzle selection is enabled
     */
    boolean isRandomPuzzleSelectionEnabled();

    /**
     * Changes whether random puzzle selection is enabled.
     *
     * @requires true
     * @ensures isRandomPuzzleSelectionEnabled() == enabled
     *
     * @param enabled the new random puzzle setting
     */
    void setRandomPuzzleSelectionEnabled(boolean enabled);

    /**
     * Checks the model invariants.
     *
     * @requires true
     * @ensures the model is not changed
     *
     * @return true if the invariants hold
     */
    boolean invariant();

    /**
     * Adds an observer that is notified when the model changes.
     *
     * @requires observer != null
     * @ensures observer is registered with the model
     *
     * @param observer the observer to add
     */
    void addObserver(Observer observer);
}
