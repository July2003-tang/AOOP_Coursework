package sudoku.model;

import java.util.List;
import java.util.Observable;
import java.util.Random;

// Main model class shared by both GUI and CLI.
// @invariant board != null && solutionBoard != null && puzzles != null
// @invariant every board value is in the range 0..9
// @invariant every fixed cell keeps its original puzzle value
public class SudokuModel extends Observable implements SudokuGameModel {
    private static final int SIZE = 9;

    // Current board and solved copy used for hints.
    private Board board;
    private Board solutionBoard;

    private final List<Board> puzzles;
    private final Random random;
    private Move lastMove;

    // Required model flags.
    private boolean validationFeedbackFlag;
    private boolean hintFlag;
    private boolean puzzleSelectionFlag;

    public SudokuModel(String puzzleFile) {
        // Load puzzles once, then choose one according to the selection flag.
        this.puzzles = PuzzleLoader.loadPuzzles(puzzleFile);
        this.random = new Random();

        this.validationFeedbackFlag = true;
        this.hintFlag = true;
        this.puzzleSelectionFlag = false;

        loadNewBoard();
        assert hasValidState();
    }

    // @requires true
    // @ensures invalid row/col ==> result == 0
    // @ensures valid row/col ==> result == the value stored at that cell
    // @ensures result >= 0 && result <= 9
    public int getValue(int row, int col) {
        int result;
        if (!isValidPosition(row, col)) {
            result = 0;
        } else {
            result = board.getValue(row, col);
        }

        assert result >= 0 && result <= 9;
        assert hasValidState();
        return result;
    }

    // @requires true
    // @ensures result == true ==> row and col are inside the board
    // @ensures result == true ==> the selected cell is editable
    public boolean isEditable(int row, int col) {
        boolean result;
        if (!isValidPosition(row, col)) {
            result = false;
        } else {
            result = board.isEditable(row, col);
        }

        assert !result || isValidPosition(row, col);
        assert hasValidState();
        return result;
    }

    // @requires true
    // @ensures result == true ==> row and col are valid, the cell is editable, and 1 <= value <= 9
    // @ensures result == true ==> getValue(row, col) == value
    // @ensures result == false ==> invalid positions, fixed cells, invalid values, or unchanged values are rejected
    // @ensures temporary duplicate values may be stored; validation is checked separately
    public boolean setValue(int row, int col, int value) {
        // The model rejects bad positions, fixed cells and non-digit values.
        if (!isValidPosition(row, col) || value < 1 || value > 9) {
            assert hasValidState();
            return false;
        }

        if (!board.isEditable(row, col)) {
            assert hasValidState();
            return false;
        }

        int oldValue = board.getValue(row, col);

        if (oldValue == value) {
            assert hasValidState();
            return false;
        }

        if (oldValue != 0) {
            board.clearValue(row, col);
        }

        // Duplicate values are allowed temporarily; validation feedback handles warnings.
        board.setValue(row, col, value);
        lastMove = new Move(row, col, oldValue, value);

        assert board.getValue(row, col) == value;
        assert hasValidState();
        notifyModelChanged();
        return true;
    }

    // @requires true
    // @ensures result == true ==> row and col are valid and the editable cell becomes empty
    // @ensures result == false ==> invalid positions, fixed cells, and already empty cells are rejected
    public boolean clearValue(int row, int col) {
        if (!isValidPosition(row, col)) {
            assert hasValidState();
            return false;
        }

        if (!board.isEditable(row, col)) {
            assert hasValidState();
            return false;
        }

        int oldValue = board.getValue(row, col);

        if (oldValue == 0) {
            assert hasValidState();
            return false;
        }

        board.clearValue(row, col);
        lastMove = new Move(row, col, oldValue, 0);

        assert board.getValue(row, col) == 0;
        assert hasValidState();
        notifyModelChanged();
        return true;
    }

    // @requires true
    // @ensures result == true ==> the most recent board action is reverted
    // @ensures result == true ==> hasUndo() == false
    // @ensures result == false ==> there was no stored move to undo
    public boolean undo() {
        // Coursework only asks for single-level undo.
        if (lastMove == null) {
            assert hasValidState();
            return false;
        }

        Move moveToUndo = lastMove;
        lastMove = null;

        int row = moveToUndo.getRow();
        int col = moveToUndo.getCol();
        int oldValue = moveToUndo.getOldValue();

        if (!board.isEditable(row, col)) {
            assert hasValidState();
            return false;
        }

        if (oldValue == 0) {
            board.clearValue(row, col);
        } else {
            board.setValue(row, col, oldValue);
        }

        assert board.getValue(row, col) == oldValue;
        assert lastMove == null;
        assert hasValidState();
        notifyModelChanged();
        return true;
    }

    // @requires true
    // @ensures result == true ==> exactly one empty editable cell is filled with its solution value
    // @ensures result == false ==> hint is disabled or no empty editable cell exists
    // @ensures fixed cells are not changed
    public boolean hint() {
        // Fill the first empty editable cell with its solution value.
        if (!hintFlag) {
            assert hasValidState();
            return false;
        }

        int filledBefore = countFilledCells();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board.isEditable(row, col) && board.getValue(row, col) == 0) {
                    int correctValue = solutionBoard.getValue(row, col);

                    board.setValue(row, col, correctValue);
                    lastMove = new Move(row, col, 0, correctValue);

                    assert board.getValue(row, col) == correctValue;
                    assert countFilledCells() == filledBefore + 1;
                    assert hasValidState();
                    notifyModelChanged();
                    return true;
                }
            }
        }

        assert hasValidState();
        return false;
    }

    // @requires true
    // @ensures all editable cells are empty
    // @ensures fixed cells keep their original values
    // @ensures hasUndo() == false
    public void reset() {
        board.reset();
        lastMove = null;
        assert allEditableCellsAreEmpty();
        assert lastMove == null;
        assert hasValidState();
        notifyModelChanged();
    }

    // @requires true
    // @ensures a puzzle is loaded according to the puzzle selection flag
    // @ensures all editable cells start empty
    // @ensures hasUndo() == false
    public void newGame() {
        loadNewBoard();
        lastMove = null;
        assert allEditableCellsAreEmpty();
        assert lastMove == null;
        assert hasValidState();
        notifyModelChanged();
    }

    // @requires true
    // @ensures result == true ==> the board is full and valid according to Sudoku rules
    // @ensures result == false ==> the board is incomplete or has a duplicate value
    public boolean isComplete() {
        // A puzzle is complete only when the board is full and valid.
        boolean result = board.isSolved();
        assert !result || isBoardValid();
        assert hasValidState();
        return result;
    }

    // @requires true
    // @ensures result == true ==> no row, column or 3x3 box contains duplicate non-zero values
    // @ensures result == false ==> at least one row, column or 3x3 box has a duplicate value
    public boolean isBoardValid() {
        boolean result = true;

        for (int i = 0; i < SIZE; i++) {
            if (!board.isRowValid(i) || !board.isColumnValid(i)) {
                result = false;
                break;
            }
        }

        if (result) {
            for (int row = 0; row < SIZE; row += 3) {
                for (int col = 0; col < SIZE; col += 3) {
                    if (!board.isBoxValid(row, col)) {
                        result = false;
                        break;
                    }
                }

                if (!result) {
                    break;
                }
            }
        }

        assert hasValidState();
        return result;
    }

    // @requires true
    // @ensures result == true ==> row and col are valid, the cell is editable, and 1 <= value <= 9
    // @ensures result == true ==> placing value there does not break row, column, or 3x3 box rules
    // @ensures result == false ==> the move is not a legal Sudoku move
    public boolean isValidMove(int row, int col, int value) {
        boolean result = isValidPosition(row, col) && board.isValidMove(row, col, value);
        assert !result || (isValidPosition(row, col) && value >= 1 && value <= 9 && board.isEditable(row, col));
        assert hasValidState();
        return result;
    }

    // @requires true
    // @ensures result == true <==> one stored move is available for single-level undo
    public boolean hasUndo() {
        boolean result = lastMove != null;
        assert result == (lastMove != null);
        assert hasValidState();
        return result;
    }

    // @requires true
    // @ensures result == true ==> the selected non-empty cell duplicates a value in its row, column, or 3x3 box
    // @ensures result == false ==> the position is invalid, empty, or has no duplicate conflict
    public boolean hasConflict(int row, int col) {
        // Used by the GUI and CLI to report duplicate values.
        if (!isValidPosition(row, col)) {
            assert hasValidState();
            return false;
        }

        int value = board.getValue(row, col);
        if (value == 0) {
            assert hasValidState();
            return false;
        }

        for (int i = 0; i < SIZE; i++) {
            if (i != col && board.getValue(row, i) == value) {
                assert hasValidState();
                return true;
            }

            if (i != row && board.getValue(i, col) == value) {
                assert hasValidState();
                return true;
            }
        }

        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;

        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if ((r != row || c != col) && board.getValue(r, c) == value) {
                    assert hasValidState();
                    return true;
                }
            }
        }

        assert hasValidState();
        return false;
    }

    // @requires true
    // @ensures result != null
    public String getBoardText() {
        String result = board.toString();
        assert result != null;
        assert hasValidState();
        return result;
    }

    // Pre: none.
    // Post: returns the current validation feedback flag.
    public boolean isValidationFeedbackFlag() {
        boolean result = validationFeedbackFlag;
        assert result == validationFeedbackFlag;
        assert hasValidState();
        return result;
    }

    // Pre: none.
    // Post: validation feedback flag is updated.
    public void setValidationFeedbackFlag(boolean validationFeedbackFlag) {
        this.validationFeedbackFlag = validationFeedbackFlag;
        assert this.validationFeedbackFlag == validationFeedbackFlag;
        assert hasValidState();
        notifyModelChanged();
    }

    // Pre: none.
    // Post: returns the current hint flag.
    public boolean isHintFlag() {
        boolean result = hintFlag;
        assert result == hintFlag;
        assert hasValidState();
        return result;
    }

    // Pre: none.
    // Post: hint flag is updated.
    public void setHintFlag(boolean hintFlag) {
        this.hintFlag = hintFlag;
        assert this.hintFlag == hintFlag;
        assert hasValidState();
        notifyModelChanged();
    }

    // Pre: none.
    // Post: returns the current puzzle selection flag.
    public boolean isPuzzleSelectionFlag() {
        boolean result = puzzleSelectionFlag;
        assert result == puzzleSelectionFlag;
        assert hasValidState();
        return result;
    }

    // Pre: none.
    // Post: puzzle selection flag is updated.
    public void setPuzzleSelectionFlag(boolean puzzleSelectionFlag) {
        this.puzzleSelectionFlag = puzzleSelectionFlag;
        assert this.puzzleSelectionFlag == puzzleSelectionFlag;
        assert hasValidState();
        notifyModelChanged();
    }

    private void loadNewBoard() {
        // Fixed mode always uses the first puzzle; random mode chooses from the file.
        Board selectedPuzzle;

        if (puzzleSelectionFlag) {
            selectedPuzzle = puzzles.get(random.nextInt(puzzles.size()));
        } else {
            selectedPuzzle = puzzles.get(0);
        }

        board = selectedPuzzle.copy();

        solutionBoard = selectedPuzzle.copy();
        boolean solved = solveBoard(solutionBoard);

        if (!solved) {
            throw new IllegalStateException("The selected puzzle has no solution.");
        }
    }

    private boolean solveBoard(Board boardToSolve) {
        // Simple backtracking solver used only to prepare hints.
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (boardToSolve.isEditable(row, col) && boardToSolve.getValue(row, col) == 0) {
                    for (int value = 1; value <= 9; value++) {
                        if (boardToSolve.isValidMove(row, col, value)) {
                            boardToSolve.setValue(row, col, value);

                            if (solveBoard(boardToSolve)) {
                                return true;
                            }

                            boardToSolve.clearValue(row, col);
                        }
                    }

                    return false;
                }
            }
        }

        return boardToSolve.isSolved();
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    private int countFilledCells() {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board.getValue(row, col) != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean allEditableCellsAreEmpty() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board.isEditable(row, col) && board.getValue(row, col) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasValidState() {
        // Basic model invariant checked with assertions.
        if (board == null || solutionBoard == null || puzzles == null || puzzles.isEmpty()) {
            return false;
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int value = board.getValue(row, col);
                if (value < 0 || value > 9) {
                    return false;
                }

                if (!board.isEditable(row, col) && board.getValue(row, col) != solutionBoard.getValue(row, col)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void notifyModelChanged() {
        // Notify the Swing view after model changes.
        setChanged();
        notifyObservers();
    }
}
