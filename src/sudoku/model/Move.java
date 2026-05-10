package sudoku.model;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/11 15:55
 */
// Stores one change made by the user so it can be undone.
public final class Move {
    private final int row;
    private final int col;
    private final int oldValue;
    private final int newValue;

    // Creates a move record. Row and column use the internal 0-8 coordinates.
    public Move(int row, int col, int oldValue, int newValue) {
        this.row = row;
        this.col = col;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Returns the row changed by this move.
    public int getRow() {
        return row;
    }

    // Returns the column changed by this move.
    public int getCol() {
        return col;
    }

    // Returns the value before the user changed the cell.
    public int getOldValue() {
        return oldValue;
    }

    // Returns the value after the user changed the cell.
    public int getNewValue() {
        return newValue;
    }
}
