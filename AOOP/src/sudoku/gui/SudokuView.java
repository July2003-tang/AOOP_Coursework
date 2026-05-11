package sudoku.gui;

import sudoku.controller.SudokuController;
import sudoku.model.SudokuGameModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

// Swing view for the GUI version of Sudoku.
public class SudokuView extends JFrame implements Observer {
    // Simple colours to show fixed, editable, selected and invalid cells.
    private static final Color FIXED_CELL_COLOR = new Color(226, 232, 240);
    private static final Color EDITABLE_CELL_COLOR = Color.WHITE;
    private static final Color SELECTED_CELL_COLOR = new Color(219, 234, 254);
    private static final Color CONFLICT_CELL_COLOR = new Color(254, 202, 202);
    private static final Color GRID_COLOR = new Color(30, 41, 59);

    private final SudokuGameModel model;
    private SudokuController controller;

    private final JButton[][] cellButtons;
    private final JButton eraseButton;
    private final JButton undoButton;
    private final JButton hintButton;
    private final JCheckBox validationCheckBox;
    private final JCheckBox hintCheckBox;
    private final JCheckBox randomPuzzleCheckBox;

    private int selectedRow;
    private int selectedCol;

    public SudokuView(SudokuGameModel model) {
        super("Sudoku");
        this.model = model;
        this.cellButtons = new JButton[9][9];
        this.eraseButton = new JButton("Erase");
        this.undoButton = new JButton("Undo");
        this.hintButton = new JButton("Hint");
        this.validationCheckBox = new JCheckBox("Validation feedback", model.isValidationFeedbackFlag());
        this.hintCheckBox = new JCheckBox("Hints", model.isHintFlag());
        this.randomPuzzleCheckBox = new JCheckBox("Random puzzles", model.isPuzzleSelectionFlag());

        this.selectedRow = 0;
        this.selectedCol = 0;

        // The view refreshes whenever the model changes.
        model.addObserver(this);
        buildWindow();
        registerKeyboardActions();
        refreshBoard();
    }

    public void setController(SudokuController controller) {
        // The controller is attached after the view has been created.
        this.controller = controller;
        wireActions();
        controller.updateControls();
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void setSelectedCell(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        refreshBoard();
    }

    public void setEraseEnabled(boolean enabled) {
        eraseButton.setEnabled(enabled);
    }

    public void setUndoEnabled(boolean enabled) {
        undoButton.setEnabled(enabled);
    }

    public void setHintButtonEnabled(boolean enabled) {
        hintButton.setEnabled(enabled);
    }

    public void showMessage(String message) {
        JOptionPane.showOptionDialog(
                this,
                message,
                "Message",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"OK"},
                "OK");
    }

    public void showCompletionMessage() {
        JOptionPane.showOptionDialog(
                this,
                "Congratulations! You completed the puzzle correctly.",
                "Completed",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"OK"},
                "OK");
    }

    @Override
    public void update(Observable observable, Object value) {
        // Called by the model after a board or option change.
        refreshBoard();
        validationCheckBox.setSelected(model.isValidationFeedbackFlag());
        hintCheckBox.setSelected(model.isHintFlag());
        randomPuzzleCheckBox.setSelected(model.isPuzzleSelectionFlag());

        if (controller != null) {
            controller.updateControls();
        }
    }

    private void buildWindow() {
        // Main window layout: board in the centre, controls on the right.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JLabel titleLabel = new JLabel("Sudoku", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        add(createBoardPanel(), BorderLayout.CENTER);
        add(createSidePanel(), BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(720, 560));
    }

    private JPanel createBoardPanel() {
        // Create the 9x9 grid of buttons.
        JPanel boardPanel = new JPanel(new GridLayout(9, 9));
        boardPanel.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 3));

        Font cellFont = new Font(Font.SANS_SERIF, Font.BOLD, 22);

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                JButton cellButton = new JButton();
                cellButton.setFont(cellFont);
                cellButton.setFocusPainted(false);
                cellButton.setPreferredSize(new Dimension(54, 54));
                cellButton.setOpaque(true);
                cellButtons[row][col] = cellButton;
                boardPanel.add(cellButton);
            }
        }

        return boardPanel;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout(8, 8));
        sidePanel.add(createVirtualKeyboardPanel(), BorderLayout.NORTH);
        sidePanel.add(createButtonPanel(), BorderLayout.CENTER);
        sidePanel.add(createFlagPanel(), BorderLayout.SOUTH);
        return sidePanel;
    }

    private JPanel createVirtualKeyboardPanel() {
        // On-screen keyboard only contains digits 1-9.
        JPanel keyboardPanel = new JPanel(new GridLayout(3, 3, 4, 4));
        keyboardPanel.setBorder(BorderFactory.createTitledBorder("Keyboard"));

        for (int value = 1; value <= 9; value++) {
            JButton keyButton = new JButton(String.valueOf(value));
            keyButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            final int digit = value;
            keyButton.addActionListener(event -> {
                if (controller != null) {
                    controller.enterValue(digit);
                }
            });
            keyboardPanel.add(keyButton);
        }

        return keyboardPanel;
    }

    private JPanel createButtonPanel() {
        // Main game controls required by the coursework.
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 4, 4));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        JButton resetButton = new JButton("Reset");
        JButton newGameButton = new JButton("New Game");

        eraseButton.addActionListener(event -> controller.eraseSelectedCell());
        undoButton.addActionListener(event -> controller.undo());
        hintButton.addActionListener(event -> controller.hint());
        resetButton.addActionListener(event -> controller.reset());
        newGameButton.addActionListener(event -> controller.newGame());

        buttonPanel.add(eraseButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(hintButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(newGameButton);

        return buttonPanel;
    }

    private JPanel createFlagPanel() {
        // These check boxes change the three model flags at runtime.
        JPanel flagPanel = new JPanel(new GridLayout(3, 1));
        flagPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        validationCheckBox.addActionListener(event -> controller.setValidationFeedback(validationCheckBox.isSelected()));
        hintCheckBox.addActionListener(event -> controller.setHintEnabled(hintCheckBox.isSelected()));
        randomPuzzleCheckBox.addActionListener(event -> controller.setRandomPuzzleSelection(randomPuzzleCheckBox.isSelected()));

        flagPanel.add(validationCheckBox);
        flagPanel.add(hintCheckBox);
        flagPanel.add(randomPuzzleCheckBox);

        return flagPanel;
    }

    private void wireActions() {
        // Mouse clicks select a cell.
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                final int selectedButtonRow = row;
                final int selectedButtonCol = col;
                cellButtons[row][col].addActionListener(event -> controller.selectCell(selectedButtonRow, selectedButtonCol));
            }
        }
    }

    private void registerKeyboardActions() {
        // Physical keyboard input uses the same controller methods as buttons.
        JPanel rootPanel = (JPanel) getContentPane();

        for (int value = 1; value <= 9; value++) {
            final int digit = value;
            rootPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(String.valueOf(value)), "digit" + value);
            rootPanel.getActionMap().put("digit" + value, new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    if (controller != null) {
                        controller.enterValue(digit);
                    }
                }
            });
        }

        rootPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "erase");
        rootPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "erase");
        rootPanel.getActionMap().put("erase", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (controller != null) {
                    controller.eraseSelectedCell();
                }
            }
        });

        rootPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        rootPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        rootPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        rootPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");

        rootPanel.getActionMap().put("up", movementAction(-1, 0));
        rootPanel.getActionMap().put("down", movementAction(1, 0));
        rootPanel.getActionMap().put("left", movementAction(0, -1));
        rootPanel.getActionMap().put("right", movementAction(0, 1));
    }

    private javax.swing.Action movementAction(int rowChange, int colChange) {
        return new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (controller != null) {
                    controller.moveSelection(rowChange, colChange);
                }
            }
        };
    }

    private void refreshBoard() {
        // Copy the model state into the visible buttons.
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                JButton cellButton = cellButtons[row][col];
                int value = model.getValue(row, col);

                cellButton.setText(value == 0 ? "" : String.valueOf(value));
                cellButton.setForeground(model.isEditable(row, col) ? new Color(15, 23, 42) : new Color(71, 85, 105));
                cellButton.setBackground(cellBackground(row, col));
                cellButton.setBorder(BorderFactory.createMatteBorder(
                        row % 3 == 0 ? 3 : 1,
                        col % 3 == 0 ? 3 : 1,
                        row == 8 ? 3 : 1,
                        col == 8 ? 3 : 1,
                        GRID_COLOR));
            }
        }
    }

    private Color cellBackground(int row, int col) {
        // Invalid cells are highlighted only when validation feedback is on.
        if (model.isValidationFeedbackFlag() && model.hasConflict(row, col)) {
            return CONFLICT_CELL_COLOR;
        }

        if (row == selectedRow && col == selectedCol) {
            return SELECTED_CELL_COLOR;
        }

        return model.isEditable(row, col) ? EDITABLE_CELL_COLOR : FIXED_CELL_COLOR;
    }
}
