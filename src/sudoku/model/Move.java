package sudoku.model;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/11 15:55
 */
/**
 * Stores one change made by the user so it can be undone.
 */
public final class Move {
    private final int row;
    private final int col;
    private final int oldValue;
    private final int newValue;

    /**
     * Creates a move record.
     *
     * @param row the changed row using the internal 0-8 coordinate system
     * @param col the changed column using the internal 0-8 coordinate system
     * @param oldValue the value before the move
     * @param newValue the value after the move
     */
    public Move(int row, int col, int oldValue, int newValue) {
        this.row = row;
        this.col = col;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the row changed by this move.
     *
     * @return the changed row
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column changed by this move.
     *
     * @return the changed column
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns the value before the user changed the cell.
     *
     * @return the old value
     */
    public int getOldValue() {
        return oldValue;
    }

    /**
     * Returns the value after the user changed the cell.
     *
     * @return the new value
     */
    public int getNewValue() {
        return newValue;
    }
}
