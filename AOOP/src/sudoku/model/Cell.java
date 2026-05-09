package sudoku.model;

// One square on the Sudoku board.
// 0 means empty, and a non-zero initial value makes the cell fixed.
public class Cell {

    // current value of the cell (0 means empty)
    private int value;

    // initial value from the puzzle (fixed if not 0)
    private final int fixedValue;

    // whether the cell can be edited by the player
    private final boolean editable;

    // constructor: initialize cell with a value from puzzle
    public Cell(int initialValue) {
        if (initialValue < 0 || initialValue > 9) {
            throw new IllegalArgumentException("Cell value must be between 0 and 9.");
        }

        this.value = initialValue;
        this.fixedValue = initialValue;

        // only empty cells are editable
        this.editable = (initialValue == 0);
    }

    // get current value
    public int getValue() {
        return value;
    }

    // set value (only for editable cells)
    public void setValue(int value) {
        if (!editable) {
            throw new IllegalStateException("Cannot modify a fixed cell.");
        }

        if (value < 1 || value > 9) {
            throw new IllegalArgumentException("Cell value must be between 1 and 9.");
        }

        this.value = value;
    }

    // clear the cell (set to 0)
    public void clear() {
        if (!editable) {
            throw new IllegalStateException("Cannot clear a fixed cell.");
        }

        this.value = 0;
    }

    // check if cell can be edited
    public boolean isEditable() {
        return editable;
    }

    // check if cell is empty
    public boolean isEmpty() {
        return value == 0;
    }

    // check if cell is fixed (given by puzzle)
    public boolean isFixed() {
        return !editable;
    }

    // reset cell back to initial puzzle value
    public void reset() {
        this.value = fixedValue;
    }

    // used for printing board (CLI)
    @Override
    public String toString() {
        return value == 0 ? "." : String.valueOf(value);
    }
}
