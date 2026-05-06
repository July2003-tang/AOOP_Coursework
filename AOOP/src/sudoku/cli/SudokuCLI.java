package sudoku.cli;

import sudoku.model.SudokuModel;

import java.util.Scanner;

// Command line version of the Sudoku game.
public class SudokuCLI {
    private static final String PUZZLE_FILE = "src/resources/puzzles.txt";

    private final SudokuModel model;
    private final Scanner scanner;

    public SudokuCLI() {
        // CLI uses the model directly, without a separate view or controller.
        model = new SudokuModel(PUZZLE_FILE);
        model.setPuzzleSelectionFlag(true);
        model.setValidationFeedbackFlag(true);
        model.newGame();
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        SudokuCLI cli = new SudokuCLI();
        cli.start();
    }

    public void start() {
        // Main input loop for the game.
        printWelcomeMessage();

        while (true) {
            printBoard();

            if (model.isComplete()) {
                System.out.println("Congratulations! You completed the puzzle correctly.");
                System.out.println("Type 'new' to start a new puzzle or 'quit' to exit.");
            }

            System.out.print("Enter command: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            processCommand(input);
        }
    }

    private void printWelcomeMessage() {
        System.out.println("Welcome to Sudoku CLI!");
        System.out.println("Type 'help' to see all available commands.");
        System.out.println();
        printHelp();
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  set row col value  - Put a number into a cell.");
        System.out.println("                       Example: set 1 3 4");
        System.out.println("  clear row col      - Clear an editable cell.");
        System.out.println("                       Example: clear 1 3");
        System.out.println("  undo               - Undo the most recent move.");
        System.out.println("  hint               - Fill one empty cell with a correct value.");
        System.out.println("  reset              - Reset the current puzzle.");
        System.out.println("  new                - Start a new puzzle.");
        System.out.println("  check              - Check whether the current board is valid.");
        System.out.println("  help               - Show this help message.");
        System.out.println("  quit               - Exit the game.");
        System.out.println();
        System.out.println("Note: rows and columns must be numbers from 1 to 9.");
        System.out.println();
    }

    private void processCommand(String input) {
        // Split the input so commands like "set 1 3 4" can be handled.
        if (input.isEmpty()) {
            System.out.println("No command entered. Type 'help' to see available commands.");
            return;
        }

        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "set":
                    handleSet(parts);
                    break;

                case "clear":
                    handleClear(parts);
                    break;

                case "undo":
                    handleUndo(parts);
                    break;

                case "hint":
                    handleHint(parts);
                    break;

                case "reset":
                    handleReset(parts);
                    break;

                case "new":
                    handleNewGame(parts);
                    break;

                case "check":
                    handleCheck(parts);
                    break;

                case "help":
                    handleHelp(parts);
                    break;

                default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Type 'help' to see available commands.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Row, column, and value must be numbers.");
            System.out.println("Example: set 1 3 4");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private void handleSet(String[] parts) {
        // set row col value
        if (parts.length != 4) {
            System.out.println("Invalid set command.");
            System.out.println("Usage: set row col value");
            System.out.println("Example: set 1 3 4");
            return;
        }

        int row = Integer.parseInt(parts[1]) - 1;
        int col = Integer.parseInt(parts[2]) - 1;
        int value = Integer.parseInt(parts[3]);

        if (!isValidUserPosition(row, col)) {
            System.out.println("Invalid position. Row and column must be between 1 and 9.");
            return;
        }

        if (value < 1 || value > 9) {
            System.out.println("Invalid value. Value must be between 1 and 9.");
            return;
        }

        if (!model.isEditable(row, col)) {
            System.out.println("Move rejected. This cell is fixed and cannot be changed.");
            return;
        }

        boolean success = model.setValue(row, col, value);

        if (success) {
            System.out.println("Value entered successfully.");

            // The value is kept on the board, but the player is warned if it conflicts.
            if (model.isValidationFeedbackFlag() && model.hasConflict(row, col)) {
                System.out.println("Warning: the value creates duplicate numbers.");
                System.out.println("Please check the row, column, or 3x3 box.");
                System.out.println("You can use 'clear row col' or 'undo' to fix it.");
            }
        } else {
            System.out.println("Move rejected. The value may be unchanged.");
        }
    }

    private void handleClear(String[] parts) {
        // clear row col
        if (parts.length != 3) {
            System.out.println("Invalid clear command.");
            System.out.println("Usage: clear row col");
            System.out.println("Example: clear 1 3");
            return;
        }

        int row = Integer.parseInt(parts[1]) - 1;
        int col = Integer.parseInt(parts[2]) - 1;

        if (!isValidUserPosition(row, col)) {
            System.out.println("Invalid position. Row and column must be between 1 and 9.");
            return;
        }

        if (!model.isEditable(row, col)) {
            System.out.println("Clear rejected. This cell is fixed and cannot be cleared.");
            return;
        }

        if (model.getValue(row, col) == 0) {
            System.out.println("Clear rejected. This cell is already empty.");
            return;
        }

        boolean success = model.clearValue(row, col);

        if (success) {
            System.out.println("Cell cleared.");
        } else {
            System.out.println("Clear failed.");
        }
    }

    private void handleUndo(String[] parts) {
        // Undo only stores the most recent board action.
        if (parts.length != 1) {
            System.out.println("Invalid undo command.");
            System.out.println("Usage: undo");
            return;
        }

        boolean success = model.undo();

        if (success) {
            System.out.println("Last move undone.");
        } else {
            System.out.println("There is no move to undo.");
        }
    }

    private void handleHint(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Invalid hint command.");
            System.out.println("Usage: hint");
            return;
        }

        boolean success = model.hint();

        if (success) {
            System.out.println("Hint applied. One correct value has been filled.");
        } else {
            System.out.println("Hint failed. Hint may be disabled or no empty editable cell is available.");
        }
    }

    private void handleReset(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Invalid reset command.");
            System.out.println("Usage: reset");
            return;
        }

        model.reset();
        System.out.println("Puzzle reset. All user-entered values have been cleared.");
    }

    private void handleNewGame(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Invalid new command.");
            System.out.println("Usage: new");
            return;
        }

        model.newGame();
        System.out.println("New game started.");
    }

    private void handleCheck(String[] parts) {
        // Let the player check the current board at any time.
        if (parts.length != 1) {
            System.out.println("Invalid check command.");
            System.out.println("Usage: check");
            return;
        }

        if (model.isBoardValid()) {
            System.out.println("The current board is valid so far.");
        } else {
            System.out.println("The current board contains duplicate numbers.");
        }

        if (model.isComplete()) {
            System.out.println("The puzzle is complete and correct.");
        } else {
            System.out.println("The puzzle is not completed yet.");
        }
    }

    private void handleHelp(String[] parts) {
        if (parts.length != 1) {
            System.out.println("Invalid help command.");
            System.out.println("Usage: help");
            return;
        }

        printHelp();
    }

    private boolean isValidUserPosition(int row, int col) {
        return row >= 0 && row < 9 && col >= 0 && col < 9;
    }

    private void printBoard() {
        // Board formatting is provided by the model.
        System.out.println();
        System.out.println(model.getBoardText());
    }
}
