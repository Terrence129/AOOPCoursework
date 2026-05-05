package sudoku.model;

import java.util.Objects;

/**
 * @description:
 * @author: chenyaqi
 * @email: terrence.yaqi.chen@u.nus.edu
 * @date: 2026/5/11 15:53
 */
// Represents one cell on the Sudoku board.
public final class Cell {
    private final int value;
    private final boolean isPreFilled;

    // Creates a cell. Value 0 means the cell is empty.
    public Cell(int value, boolean isPreFilled) {
        this.value = value;
        this.isPreFilled = isPreFilled;
    }

    // Returns the number stored in this cell.
    public int getValue() {
        return value;
    }

    // Returns true if this cell was filled in the original puzzle.
    public boolean isPreFilled() {
        return isPreFilled;
    }

    // Checks whether another object has the same cell data.
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Cell)) {
            return false;
        }
        Cell cell = (Cell) object;
        return value == cell.value && isPreFilled == cell.isPreFilled;
    }

    // Creates a hash code using the same data checked in equals.
    @Override
    public int hashCode() {
        return Objects.hash(value, isPreFilled);
    }
}
