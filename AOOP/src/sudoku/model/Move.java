package sudoku.model;

// Stores one user action for single-level undo.
// The model only keeps the latest Move, not a whole history stack.
public class Move {

    // row index of the move (0-8)
    private final int row;

    // column index of the move (0-8)
    private final int col;

    // value before the move
    private final int oldValue;

    // value after the move
    private final int newValue;

    // constructor: record a single move
    public Move(int row, int col, int oldValue, int newValue) {

        // check position range
        if (row < 0 || row > 8 || col < 0 || col > 8) {
            throw new IllegalArgumentException("Row and column must be between 0 and 8.");
        }

        // check value range (0 means empty)
        if (oldValue < 0 || oldValue > 9 || newValue < 0 || newValue > 9) {
            throw new IllegalArgumentException("Move values must be between 0 and 9.");
        }

        this.row = row;
        this.col = col;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // get row index
    public int getRow() {
        return row;
    }

    // get column index
    public int getCol() {
        return col;
    }

    // get value before the move
    public int getOldValue() {
        return oldValue;
    }

    // get value after the move
    public int getNewValue() {
        return newValue;
    }
}
