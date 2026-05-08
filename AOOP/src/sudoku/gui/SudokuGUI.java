package sudoku.gui;

import sudoku.controller.SudokuController;
import sudoku.model.SudokuModel;

import javax.swing.SwingUtilities;

// Main class for starting the Swing version of the game.
public class SudokuGUI {
    private static final String PUZZLE_FILE = "src/resources/puzzles.txt";

    public static void main(String[] args) {
        // Swing windows should be created on the event dispatch thread.
        SwingUtilities.invokeLater(() -> {
            SudokuModel model = new SudokuModel(PUZZLE_FILE);
            SudokuView view = new SudokuView(model);
            SudokuController controller = new SudokuController(model, view);
            view.setController(controller);
            view.setVisible(true);
        });
    }
}
