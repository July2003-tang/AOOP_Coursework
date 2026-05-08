package sudoku.controller;

import sudoku.gui.SudokuView;
import sudoku.model.SudokuModel;

// The controller connects the GUI actions to the model.
public class SudokuController {
    private final SudokuModel model;
    private final SudokuView view;

    public SudokuController(SudokuModel model, SudokuView view) {
        this.model = model;
        this.view = view;
        updateControls();
    }

    public void selectCell(int row, int col) {
        // Ignore positions outside the board.
        if (row < 0 || row >= 9 || col < 0 || col >= 9) {
            return;
        }

        view.setSelectedCell(row, col);
        updateControls();
    }

    public void moveSelection(int rowChange, int colChange) {
        // Keep keyboard movement inside the 9x9 grid.
        int row = Math.max(0, Math.min(8, view.getSelectedRow() + rowChange));
        int col = Math.max(0, Math.min(8, view.getSelectedCol() + colChange));
        selectCell(row, col);
    }

    public void enterValue(int value) {
        int row = view.getSelectedRow();
        int col = view.getSelectedCol();

        // Only digits 1-9 should be passed to the model.
        if (value < 1 || value > 9) {
            view.showMessage("Only digits 1-9 are valid inputs.");
            return;
        }

        if (!model.isEditable(row, col)) {
            view.showMessage("This cell is fixed and cannot be changed.");
            return;
        }

        // Temporary duplicate values are allowed; the view will highlight them.
        if (model.setValue(row, col, value)) {
            checkCompletion();
        }
    }

    public void eraseSelectedCell() {
        int row = view.getSelectedRow();
        int col = view.getSelectedCol();

        if (!model.isEditable(row, col)) {
            view.showMessage("This cell is fixed and cannot be erased.");
            return;
        }

        model.clearValue(row, col);
    }

    public void undo() {
        if (!model.undo()) {
            view.showMessage("There is no move to undo.");
        }
    }

    public void hint() {
        if (!model.hint()) {
            view.showMessage("Hint is disabled or no empty editable cell is available.");
            return;
        }

        checkCompletion();
    }

    public void reset() {
        model.reset();
    }

    public void newGame() {
        model.newGame();
        view.setSelectedCell(0, 0);
    }

    public void setValidationFeedback(boolean enabled) {
        model.setValidationFeedbackFlag(enabled);
    }

    public void setHintEnabled(boolean enabled) {
        model.setHintFlag(enabled);
    }

    public void setRandomPuzzleSelection(boolean enabled) {
        model.setPuzzleSelectionFlag(enabled);
    }

    public void updateControls() {
        // Buttons are enabled or disabled from the current model state.
        view.setEraseEnabled(model.isEditable(view.getSelectedRow(), view.getSelectedCol())
                && model.getValue(view.getSelectedRow(), view.getSelectedCol()) != 0);
        view.setUndoEnabled(model.hasUndo());
        view.setHintButtonEnabled(model.isHintFlag());
    }

    private void checkCompletion() {
        // The model decides whether the puzzle is solved.
        if (model.isComplete()) {
            view.showCompletionMessage();
        }
    }
}
