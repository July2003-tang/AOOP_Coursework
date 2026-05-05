package sudoku.model;

import java.util.List;
import java.util.Observable;
import java.util.Random;

// Main model class shared by both GUI and CLI.
public class SudokuModel extends Observable {
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

    public int getValue(int row, int col) {
        if (!isValidPosition(row, col)) {
            return 0;
        }

        return board.getValue(row, col);
    }

    public boolean isEditable(int row, int col) {
        if (!isValidPosition(row, col)) {
            return false;
        }

        return board.isEditable(row, col);
    }

    public boolean setValue(int row, int col, int value) {
        // The model rejects bad positions, fixed cells and non-digit values.
        if (!isValidPosition(row, col) || value < 1 || value > 9) {
            return false;
        }

        if (!board.isEditable(row, col)) {
            return false;
        }

        int oldValue = board.getValue(row, col);

        if (oldValue == value) {
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

    public boolean clearValue(int row, int col) {
        if (!isValidPosition(row, col)) {
            return false;
        }

        if (!board.isEditable(row, col)) {
            return false;
        }

        int oldValue = board.getValue(row, col);

        if (oldValue == 0) {
            return false;
        }

        board.clearValue(row, col);
        lastMove = new Move(row, col, oldValue, 0);

        assert board.getValue(row, col) == 0;
        assert hasValidState();
        notifyModelChanged();
        return true;
    }

    public boolean undo() {
        // Coursework only asks for single-level undo.
        if (lastMove == null) {
            return false;
        }

        Move moveToUndo = lastMove;
        lastMove = null;

        int row = moveToUndo.getRow();
        int col = moveToUndo.getCol();
        int oldValue = moveToUndo.getOldValue();

        if (!board.isEditable(row, col)) {
            return false;
        }

        if (oldValue == 0) {
            board.clearValue(row, col);
        } else {
            board.setValue(row, col, oldValue);
        }

        assert board.getValue(row, col) == oldValue;
        assert hasValidState();
        notifyModelChanged();
        return true;
    }

    public boolean hint() {
        // Fill the first empty editable cell with its solution value.
        if (!hintFlag) {
            return false;
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board.isEditable(row, col) && board.getValue(row, col) == 0) {
                    int correctValue = solutionBoard.getValue(row, col);

                    board.setValue(row, col, correctValue);
                    lastMove = new Move(row, col, 0, correctValue);

                    assert board.getValue(row, col) == correctValue;
                    assert hasValidState();
                    notifyModelChanged();
                    return true;
                }
            }
        }

        return false;
    }

    public void reset() {
        board.reset();
        lastMove = null;
        assert hasValidState();
        notifyModelChanged();
    }

    public void newGame() {
        loadNewBoard();
        lastMove = null;
        assert hasValidState();
        notifyModelChanged();
    }

    public boolean isComplete() {
        // A puzzle is complete only when the board is full and valid.
        return board.isSolved();
    }

    public boolean isBoardValid() {
        for (int i = 0; i < SIZE; i++) {
            if (!board.isRowValid(i) || !board.isColumnValid(i)) {
                return false;
            }
        }

        for (int row = 0; row < SIZE; row += 3) {
            for (int col = 0; col < SIZE; col += 3) {
                if (!board.isBoxValid(row, col)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isValidMove(int row, int col, int value) {
        if (!isValidPosition(row, col)) {
            return false;
        }
        return board.isValidMove(row, col, value);
    }

    public boolean hasUndo() {
        return lastMove != null;
    }

    public boolean hasConflict(int row, int col) {
        // Used by the GUI and CLI to report duplicate values.
        if (!isValidPosition(row, col)) {
            return false;
        }

        int value = board.getValue(row, col);
        if (value == 0) {
            return false;
        }

        for (int i = 0; i < SIZE; i++) {
            if (i != col && board.getValue(row, i) == value) {
                return true;
            }

            if (i != row && board.getValue(i, col) == value) {
                return true;
            }
        }

        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;

        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if ((r != row || c != col) && board.getValue(r, c) == value) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getBoardText() {
        return board.toString();
    }

    public boolean isValidationFeedbackFlag() {
        return validationFeedbackFlag;
    }

    public void setValidationFeedbackFlag(boolean validationFeedbackFlag) {
        this.validationFeedbackFlag = validationFeedbackFlag;
        notifyModelChanged();
    }

    public boolean isHintFlag() {
        return hintFlag;
    }

    public void setHintFlag(boolean hintFlag) {
        this.hintFlag = hintFlag;
        notifyModelChanged();
    }

    public boolean isPuzzleSelectionFlag() {
        return puzzleSelectionFlag;
    }

    public void setPuzzleSelectionFlag(boolean puzzleSelectionFlag) {
        this.puzzleSelectionFlag = puzzleSelectionFlag;
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
