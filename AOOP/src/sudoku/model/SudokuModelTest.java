package sudoku.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SudokuModelTest {

    private static final String PUZZLE_FILE = "src/resources/puzzles.txt";

    /*
     * NOTE:
     * All tests assume that the FIRST puzzle in puzzles.txt is:
     *
     * 530070000
     * 600195000
     * 098000060
     * 800060003
     * 400803001
     * 700020006
     * 060000280
     * 000419005
     * 000080079
     *
     * And its solution is:
     *
     * 534678912
     * 672195348
     * 198342567
     * 859761423
     * 426853791
     * 713924856
     * 961537284
     * 287419635
     * 345286179
     */

    private static final int[][] FIRST_PUZZLE_SOLUTION = {
            {5,3,4,6,7,8,9,1,2},
            {6,7,2,1,9,5,3,4,8},
            {1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},
            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},
            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
    };

    @Test
    public void testEditableCellCanBeChanged() {
        // Scenario:
        // Cell (0,2) is empty in the puzzle.
        // A valid number is entered.
        // Expected:
        // Value should be updated.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        boolean result = model.setValue(0, 2, 4);

        assertTrue(result);
        assertEquals(4, model.getValue(0, 2));
    }

    @Test
    public void testFixedCellCannotBeChanged() {
        // Scenario:
        // Cell (0,0) is fixed.
        // Attempt to modify it.
        // Expected:
        // Operation is rejected.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        int original = model.getValue(0, 0);
        boolean result = model.setValue(0, 0, 9);

        assertFalse(result);
        assertEquals(original, model.getValue(0, 0));
    }

    @Test
    public void testInvalidMoveAllowedWithWarningStateWhenValidationEnabled() {
        // Scenario:
        // Validation flag is ON.
        // Insert a duplicate in a row.
        // Expected:
        // Move is stored, and the board can then report that it is invalid.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);
        model.setValidationFeedbackFlag(true);

        boolean result = model.setValue(0, 2, 5);

        assertTrue(result);
        assertEquals(5, model.getValue(0, 2));
        assertFalse(model.isBoardValid());
    }

    @Test
    public void testInvalidMoveAllowedWhenValidationDisabled() {
        // Scenario:
        // Validation flag is OFF.
        // Insert invalid number.
        // Expected:
        // Move allowed but board not complete.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);
        model.setValidationFeedbackFlag(false);

        boolean result = model.setValue(0, 2, 5);

        assertTrue(result);
        assertEquals(5, model.getValue(0, 2));
        assertFalse(model.isBoardValid());
        assertFalse(model.isComplete());
    }

    @Test
    public void testUndoRestoresPreviousValue() {
        // Scenario:
        // Enter a value then undo.
        // Expected:
        // Cell returns to original.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        model.setValue(0, 2, 4);
        boolean undone = model.undo();

        assertTrue(undone);
        assertEquals(0, model.getValue(0, 2));
    }

    @Test
    public void testUndoFailsWhenNoHistory() {
        // Scenario:
        // Undo without any move.
        // Expected:
        // Return false.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        assertFalse(model.undo());
    }

    @Test
    public void testUndoIsSingleLevelOnly() {
        // Scenario:
        // Enter one value and undo it once.
        // Expected:
        // The first undo succeeds, but a second undo is rejected because only one level is stored.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        model.setValue(0, 2, 4);

        assertTrue(model.undo());
        assertFalse(model.undo());
        assertEquals(0, model.getValue(0, 2));
    }

    @Test
    public void testResetClearsUserInput() {
        // Scenario:
        // Enter value then reset.
        // Expected:
        // Board restored.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        model.setValue(0, 2, 4);
        model.reset();

        assertEquals(0, model.getValue(0, 2));
    }

    @Test
    public void testHintFillsCorrectValue() {
        // Scenario:
        // Hint enabled.
        // Expected:
        // Correct solution value filled.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);
        model.setHintFlag(true);

        boolean result = model.hint();

        assertTrue(result);
        assertEquals(4, model.getValue(0, 2));
    }

    @Test
    public void testHintDoesNothingWhenDisabled() {
        // Scenario:
        // Hint disabled.
        // Expected:
        // No change.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);
        model.setHintFlag(false);

        int before = countFilled(model);
        boolean result = model.hint();
        int after = countFilled(model);

        assertFalse(result);
        assertEquals(before, after);
    }

    @Test
    public void testNewGameReloadsPuzzle() {
        // Scenario:
        // Modify board then start new game.
        // Expected:
        // Board reset to initial state.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);
        model.setPuzzleSelectionFlag(false);

        model.setValue(0, 2, 4);
        model.newGame();

        assertEquals(0, model.getValue(0, 2));
    }

    @Test
    public void testGameNotCompleteInitially() {
        // Scenario:
        // Start new puzzle.
        // Expected:
        // Not complete.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        assertFalse(model.isComplete());
    }

    @Test
    public void testGameCompleteWhenSolved() {
        // Scenario:
        // Fill correct solution.
        // Expected:
        // Game complete.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);

        fillSolution(model);

        assertTrue(model.isComplete());
    }

    @Test
    public void testFullButInvalidBoardNotComplete() {
        // Scenario:
        // Fill correct solution then break rule.
        // Expected:
        // Not complete.

        SudokuModel model = new SudokuModel(PUZZLE_FILE);
        model.setValidationFeedbackFlag(false);

        fillSolution(model);

        model.clearValue(0, 2);
        model.setValue(0, 2, 5);

        assertFalse(model.isComplete());
    }

    private void fillSolution(SudokuModel model) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (model.isEditable(r, c)) {
                    model.setValue(r, c, FIRST_PUZZLE_SOLUTION[r][c]);
                }
            }
        }
    }

    private int countFilled(SudokuModel model) {
        int count = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (model.getValue(r, c) != 0) count++;
            }
        }
        return count;
    }
}
