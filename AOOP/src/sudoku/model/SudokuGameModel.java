package sudoku.model;

import java.util.Observer;

// Interface used by the GUI, CLI and controller.
// It keeps other parts of the program dependent on the model behaviour,
// instead of depending directly on the SudokuModel implementation.
public interface SudokuGameModel {
    int getValue(int row, int col);

    boolean isEditable(int row, int col);

    boolean setValue(int row, int col, int value);

    boolean clearValue(int row, int col);

    boolean undo();

    boolean hint();

    void reset();

    void newGame();

    boolean isComplete();

    boolean isBoardValid();

    boolean isValidMove(int row, int col, int value);

    boolean hasUndo();

    boolean hasConflict(int row, int col);

    String getBoardText();

    boolean isValidationFeedbackFlag();

    void setValidationFeedbackFlag(boolean validationFeedbackFlag);

    boolean isHintFlag();

    void setHintFlag(boolean hintFlag);

    boolean isPuzzleSelectionFlag();

    void setPuzzleSelectionFlag(boolean puzzleSelectionFlag);

    void addObserver(Observer observer);
}
