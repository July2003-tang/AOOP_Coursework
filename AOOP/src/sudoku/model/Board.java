package sudoku.model;

public class Board {
    // standard Sudoku board size
    private static final int SIZE = 9;

    // size of each 3x3 box
    private static final int BOX_SIZE = 3;

    // 9x9 grid of cells
    private final Cell[][] grid;

    // create a board from initial puzzle values
    public Board(int[][] initialValues) {
        if (initialValues == null || initialValues.length != SIZE) {
            throw new IllegalArgumentException("Board must have 9 rows.");
        }

        grid = new Cell[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            if (initialValues[row] == null || initialValues[row].length != SIZE) {
                throw new IllegalArgumentException("Each row must have 9 columns.");
            }

            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new Cell(initialValues[row][col]);
            }
        }
    }

    // get the Cell object at a position
    public Cell getCell(int row, int col) {
        validatePosition(row, col);
        return grid[row][col];
    }

    // get the value at a position
    public int getValue(int row, int col) {
        return getCell(row, col).getValue();
    }

    // set a value at a position
    public void setValue(int row, int col, int value) {
        validatePosition(row, col);
        grid[row][col].setValue(value);
    }

    // clear a value at a position
    public void clearValue(int row, int col) {
        validatePosition(row, col);
        grid[row][col].clear();
    }

    // check whether a cell is editable
    public boolean isEditable(int row, int col) {
        return getCell(row, col).isEditable();
    }

    // check whether placing value at row,col follows Sudoku rules
    public boolean isValidMove(int row, int col, int value) {
        validatePosition(row, col);

        if (value < 1 || value > 9) {
            return false;
        }

        if (!isEditable(row, col)) {
            return false;
        }

        // check row and column
        for (int i = 0; i < SIZE; i++) {
            if (i != col && grid[row][i].getValue() == value) {
                return false;
            }

            if (i != row && grid[i][col].getValue() == value) {
                return false;
            }
        }

        // check 3x3 box
        int startRow = (row / BOX_SIZE) * BOX_SIZE;
        int startCol = (col / BOX_SIZE) * BOX_SIZE;

        for (int r = startRow; r < startRow + BOX_SIZE; r++) {
            for (int c = startCol; c < startCol + BOX_SIZE; c++) {
                if ((r != row || c != col) && grid[r][c].getValue() == value) {
                    return false;
                }
            }
        }

        return true;
    }

    // check if a row has no duplicate numbers
    public boolean isRowValid(int row) {
        validateIndex(row);

        boolean[] seen = new boolean[SIZE + 1];

        for (int col = 0; col < SIZE; col++) {
            int value = grid[row][col].getValue();

            // ignore empty cells
            if (value != 0) {
                if (seen[value]) {
                    return false;
                }
                seen[value] = true;
            }
        }

        return true;
    }

    // check if a column has no duplicate numbers
    public boolean isColumnValid(int col) {
        validateIndex(col);

        boolean[] seen = new boolean[SIZE + 1];

        for (int row = 0; row < SIZE; row++) {
            int value = grid[row][col].getValue();

            // ignore empty cells
            if (value != 0) {
                if (seen[value]) {
                    return false;
                }
                seen[value] = true;
            }
        }

        return true;
    }

    // check if the 3x3 box containing row,col has no duplicate numbers
    public boolean isBoxValid(int row, int col) {
        validatePosition(row, col);

        boolean[] seen = new boolean[SIZE + 1];

        int startRow = (row / BOX_SIZE) * BOX_SIZE;
        int startCol = (col / BOX_SIZE) * BOX_SIZE;

        for (int r = startRow; r < startRow + BOX_SIZE; r++) {
            for (int c = startCol; c < startCol + BOX_SIZE; c++) {
                int value = grid[r][c].getValue();

                // ignore empty cells
                if (value != 0) {
                    if (seen[value]) {
                        return false;
                    }
                    seen[value] = true;
                }
            }
        }

        return true;
    }

    // check if all cells are filled
    public boolean isComplete() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col].isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    // check if board is complete and valid
    public boolean isSolved() {
        if (!isComplete()) {
            return false;
        }

        // check all rows and columns
        for (int i = 0; i < SIZE; i++) {
            if (!isRowValid(i) || !isColumnValid(i)) {
                return false;
            }
        }

        // check all 3x3 boxes
        for (int row = 0; row < SIZE; row += BOX_SIZE) {
            for (int col = 0; col < SIZE; col += BOX_SIZE) {
                if (!isBoxValid(row, col)) {
                    return false;
                }
            }
        }

        return true;
    }

    // reset all cells to their original puzzle values
    public void reset() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].reset();
            }
        }
    }

    // create a copy of the current board
    public Board copy() {
        int[][] values = new int[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                values[row][col] = grid[row][col].getValue();
            }
        }

        return new Board(values);
    }

    // validate row and column together
    private void validatePosition(int row, int col) {
        validateIndex(row);
        validateIndex(col);
    }

    // validate a single index
    private void validateIndex(int index) {
        if (index < 0 || index >= SIZE) {
            throw new IndexOutOfBoundsException("Index must be between 0 and 8.");
        }
    }

    // print board in a readable CLI format
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int row = 0; row < SIZE; row++) {
            if (row % BOX_SIZE == 0 && row != 0) {
                builder.append("---------------------\n");
            }

            for (int col = 0; col < SIZE; col++) {
                if (col % BOX_SIZE == 0 && col != 0) {
                    builder.append("| ");
                }

                builder.append(grid[row][col].toString()).append(" ");
            }

            builder.append("\n");
        }

        return builder.toString();
    }
}