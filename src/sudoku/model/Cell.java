package sudoku.model;

import java.util.Objects;


public final class Cell {
    private final int value;
    private final boolean isPreFilled;

    /**
     * Creates a cell.
     *
     * @param value the cell value, where 0 means empty
     * @param isPreFilled true if the cell is part of the original puzzle
     */
    public Cell(int value, boolean isPreFilled) {
        this.value = value;
        this.isPreFilled = isPreFilled;
    }

    /**
     * Returns the number stored in this cell.
     *
     * @return the cell value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns whether this cell was filled in the original puzzle.
     *
     * @return true if this cell is pre-filled
     */
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

    /**
     * Creates a hash code using the same data checked in equals.
     *
     * @return the hash code for this cell
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, isPreFilled);
    }
}
