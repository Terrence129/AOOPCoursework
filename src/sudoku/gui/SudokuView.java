package sudoku.gui;

import sudoku.model.ISudokuModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

// Shows the Sudoku GUI and refreshes itself when the model changes.
public class SudokuView implements Observer {
    private static final int BOARD_SIZE = 9;
    private static final int EMPTY_VALUE = 0;
    private static final Dimension CELL_SIZE = new Dimension(55, 55);
    private static final Dimension NUMBER_BUTTON_SIZE = new Dimension(64, 58);
    private static final Color APP_BACKGROUND = new Color(220, 234, 245);
    private static final Color PANEL_BACKGROUND = new Color(181, 213, 232);
    private static final Color NORMAL_BACKGROUND = Color.WHITE;
    private static final Color PREFILLED_BACKGROUND = new Color(220, 234, 245);
    private static final Color BUTTON_BACKGROUND = Color.WHITE;
    private static final Color BUTTON_TEXT = new Color(20, 52, 75);
    private static final Color GRID_BORDER = new Color(125, 169, 196);
    private static final Color INVALID_BACKGROUND = new Color(255, 180, 180);
    private static final Color SELECTED_BACKGROUND = new Color(198, 226, 243);
    private static final Color SELECTED_BORDER = new Color(40, 110, 180);

    private final ISudokuModel model;
    private final SudokuController controller;
    private final JFrame frame;
    private final JButton[][] cellButtons;
    private final JButton eraseButton;
    private final JButton undoButton;
    private final JButton hintButton;
    private final JButton resetButton;
    private final JButton newGameButton;
    private final JCheckBox validationFeedbackCheckBox;
    private final JCheckBox hintEnabledCheckBox;
    private final JCheckBox randomPuzzleCheckBox;
    private boolean updatingView;

    /**
     * Creates the GUI view for the Sudoku model.
     *
     * @param model the model that stores game state
     * @param controller the controller that handles user actions
     */
    public SudokuView(ISudokuModel model, SudokuController controller) {
        assert model != null : "model must not be null";
        assert controller != null : "controller must not be null";

        this.model = model;
        this.controller = controller;
        this.frame = new JFrame("Sudoku");
        this.cellButtons = new JButton[BOARD_SIZE][BOARD_SIZE];
        this.eraseButton = new JButton("Erase");
        this.undoButton = new JButton("Undo");
        this.hintButton = new JButton("Hint");
        this.resetButton = new JButton("Reset");
        this.newGameButton = new JButton("New Game");
        this.validationFeedbackCheckBox = new JCheckBox("Validation Feedback");
        this.hintEnabledCheckBox = new JCheckBox("Hint Enabled");
        this.randomPuzzleCheckBox = new JCheckBox("Random Puzzle");

        setLookAndFeelColours();
        buildFrame();
        model.addObserver(this);
        controller.setView(this);
        update(null, null);
    }

    /**
     * Refreshes the full GUI after the model changes.
     *
     * @param observable the model that changed
     * @param argument optional update data
     */
    @Override
    public void update(Observable observable, Object argument) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updatingView = true;
                refreshCells();
                refreshCheckBoxes();
                updatingView = false;
                frame.revalidate();
                frame.repaint();
            }
        });
    }

    private void buildFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(APP_BACKGROUND);
        addNumberKeyBindings();
        addNavigationKeyBindings();
        addInvalidInputHandler();
        frame.add(createCheckBoxPanel(), BorderLayout.NORTH);
        frame.add(createBoardPanel(), BorderLayout.CENTER);
        frame.add(createControlPanel(), BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setLookAndFeelColours() {
        UIManager.put("Panel.background", APP_BACKGROUND);
        UIManager.put("CheckBox.background", APP_BACKGROUND);
        UIManager.put("CheckBox.foreground", BUTTON_TEXT);
        UIManager.put("Button.background", BUTTON_BACKGROUND);
        UIManager.put("Button.foreground", BUTTON_TEXT);
    }

    private void addNumberKeyBindings() {
        JRootPane rootPane = frame.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        for (int number = 1; number <= BOARD_SIZE; number++) {
            final int selectedNumber = number;
            String actionName = "number" + number;
            inputMap.put(KeyStroke.getKeyStroke(Character.forDigit(number, 10)), actionName);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + number, 0), actionName);
            actionMap.put(actionName, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    controller.onNumberInput(selectedNumber);
                }
            });
        }
    }

    private void addNavigationKeyBindings() {
        JRootPane rootPane = frame.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        addNavigationAction(inputMap, actionMap, "moveUp", KeyEvent.VK_UP, -1, 0);
        addNavigationAction(inputMap, actionMap, "moveDown", KeyEvent.VK_DOWN, 1, 0);
        addNavigationAction(inputMap, actionMap, "moveLeft", KeyEvent.VK_LEFT, 0, -1);
        addNavigationAction(inputMap, actionMap, "moveRight", KeyEvent.VK_RIGHT, 0, 1);
    }

    private void addNavigationAction(InputMap inputMap, ActionMap actionMap, String name,
                                     int keyCode, final int rowChange, final int colChange) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                controller.onSelectionMove(rowChange, colChange);
            }
        });
    }

    private void addInvalidInputHandler() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (frame.isActive() && event.getID() == KeyEvent.KEY_PRESSED && handleArrowKey(event.getKeyCode())) {
                    return true;
                }
                if (frame.isActive()
                        && event.getID() == KeyEvent.KEY_TYPED
                        && isRejectedTypedKey(event.getKeyChar())) {
                    controller.onInvalidInput(String.valueOf(event.getKeyChar()));
                    return true;
                }
                return false;
            }
        });
    }

    private boolean handleArrowKey(int keyCode) {
        if (keyCode == KeyEvent.VK_UP) {
            controller.onSelectionMove(-1, 0);
            return true;
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            controller.onSelectionMove(1, 0);
            return true;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            controller.onSelectionMove(0, -1);
            return true;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            controller.onSelectionMove(0, 1);
            return true;
        }
        return false;
    }

    private boolean isRejectedTypedKey(char keyChar) {
        return !Character.isISOControl(keyChar)
                && (keyChar < '1' || keyChar > '9');
    }

    private JPanel createCheckBoxPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 6, 8));
        styleCheckBox(validationFeedbackCheckBox);
        styleCheckBox(hintEnabledCheckBox);
        styleCheckBox(randomPuzzleCheckBox);
        validationFeedbackCheckBox.addActionListener((ActionEvent event) -> {
            if (!updatingView) {
                controller.onValidationFeedbackChanged(validationFeedbackCheckBox.isSelected());
            }
        });
        hintEnabledCheckBox.addActionListener((ActionEvent event) -> {
            if (!updatingView) {
                controller.onHintEnabledChanged(hintEnabledCheckBox.isSelected());
            }
        });
        randomPuzzleCheckBox.addActionListener((ActionEvent event) -> {
            if (!updatingView) {
                controller.onRandomPuzzleChanged(randomPuzzleCheckBox.isSelected());
            }
        });

        panel.add(validationFeedbackCheckBox);
        panel.add(hintEnabledCheckBox);
        panel.add(randomPuzzleCheckBox);
        return panel;
    }

    private JPanel createBoardPanel() {
        JPanel panel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        panel.setBackground(GRID_BORDER);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                JButton button = createCellButton(row, col);
                cellButtons[row][col] = button;
                panel.add(button);
            }
        }
        return panel;
    }

    private JButton createCellButton(final int row, final int col) {
        JButton button = new JButton();
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        button.setFocusPainted(true);
        button.setPreferredSize(CELL_SIZE);
        button.setMinimumSize(CELL_SIZE);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBackground(NORMAL_BACKGROUND);
        button.setForeground(BUTTON_TEXT);
        button.setBorder(createCellBorder(row, col));
        button.addActionListener((ActionEvent event) -> {controller.onCellClicked(row, col);});
        return button;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(APP_BACKGROUND);
        panel.add(createNumberKeyboardPanel(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createNumberKeyboardPanel() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JPanel panel = new JPanel(new GridLayout(3, 3, 4, 4));
        wrapper.setBackground(PANEL_BACKGROUND);
        panel.setBackground(PANEL_BACKGROUND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (int number = 1; number <= BOARD_SIZE; number++) {
            panel.add(createNumberButton(number));
        }
        wrapper.add(panel);
        return wrapper;
    }

    private JButton createNumberButton(final int number) {
        JButton button = new JButton(String.valueOf(number));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        button.setPreferredSize(NUMBER_BUTTON_SIZE);
        button.setMinimumSize(NUMBER_BUTTON_SIZE);
        button.setFocusPainted(false);
        styleButton(button);
        button.addActionListener((ActionEvent event) -> {controller.onNumberInput(number);});
        return button;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        styleButton(eraseButton);
        styleButton(undoButton);
        styleButton(hintButton);
        styleButton(resetButton);
        styleButton(newGameButton);
        eraseButton.addActionListener((ActionEvent event) -> {controller.onEraseClicked();});
        undoButton.addActionListener((ActionEvent event) -> {controller.onUndoClicked();});
        hintButton.addActionListener((ActionEvent event) -> {controller.onHintClicked();});
        resetButton.addActionListener((ActionEvent event) -> {controller.onResetClicked();});
        newGameButton.addActionListener((ActionEvent event) -> {controller.onNewGameClicked();});

        panel.add(eraseButton);
        panel.add(undoButton);
        panel.add(hintButton);
        panel.add(resetButton);
        panel.add(newGameButton);
        return panel;
    }

    private void styleButton(JButton button) {
        button.setBackground(BUTTON_BACKGROUND);
        button.setForeground(BUTTON_TEXT);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(GRID_BORDER));
    }

    private void styleCheckBox(JCheckBox checkBox) {
        checkBox.setBackground(APP_BACKGROUND);
        checkBox.setForeground(BUTTON_TEXT);
        checkBox.setFocusPainted(false);
    }

    /**
     * Enables or disables the Erase button.
     *
     * @param enabled true if the button should be enabled
     */
    public void setEraseButtonEnabled(boolean enabled) {
        eraseButton.setEnabled(enabled);
    }

    /**
     * Enables or disables the Undo button.
     *
     * @param enabled true if the button should be enabled
     */
    public void setUndoButtonEnabled(boolean enabled) {
        undoButton.setEnabled(enabled);
    }

    /**
     * Enables or disables the Hint button.
     *
     * @param enabled true if the button should be enabled
     */
    public void setHintButtonEnabled(boolean enabled) {
        hintButton.setEnabled(enabled);
    }

    /**
     * Shows the completion message.
     */
    public void showCompletionMessage() {
        JOptionPane.showMessageDialog(frame, "Puzzle completed!");
    }

    /**
     * Shows a message when the player types a rejected input.
     *
     * @param input the rejected input text
     */
    public void showInvalidInputMessage(String input) {
        JOptionPane.showMessageDialog(frame,
                "Only digits 1-9 are valid inputs.",
                "Invalid input",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Moves keyboard focus to the selected cell.
     */
    public void refreshSelection() {
        refreshCells();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (controller.isSelectedCell(row, col)) {
                    cellButtons[row][col].requestFocusInWindow();
                    return;
                }
            }
        }
    }

    private void refreshCells() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                refreshCell(row, col);
            }
        }
    }

    private void refreshCell(int row, int col) {
        JButton button = cellButtons[row][col];
        int value = model.getValueAt(row, col);
        boolean preFilled = model.isPreFilled(row, col);
        boolean editable = model.isEditable(row, col);

        button.setText(value == EMPTY_VALUE ? "" : String.valueOf(value));
        button.setEnabled(editable);
        button.setFocusable(editable);
        button.setBorder(createCellBorder(row, col));
        button.setForeground(BUTTON_TEXT);
        button.setFont(new Font(Font.SANS_SERIF,
                preFilled || controller.isSelectedCell(row, col) ? Font.BOLD : Font.PLAIN, 22));
        if (model.isCellInvalid(row, col)) {
            button.setBackground(INVALID_BACKGROUND);
        } else if (controller.isSelectedCell(row, col)) {
            button.setBackground(SELECTED_BACKGROUND);
        } else if (preFilled) {
            button.setBackground(PREFILLED_BACKGROUND);
        } else {
            button.setBackground(NORMAL_BACKGROUND);
        }
        button.setOpaque(true);
        if (controller.isSelectedCell(row, col)) {
            button.requestFocusInWindow();
        }
    }

    private void refreshCheckBoxes() {
        validationFeedbackCheckBox.setSelected(model.isValidationFeedbackEnabled());
        hintEnabledCheckBox.setSelected(model.isHintEnabled());
        randomPuzzleCheckBox.setSelected(model.isRandomPuzzleSelectionEnabled());
    }

    private javax.swing.border.Border createCellBorder(int row, int col) {
        int top = row % 3 == 0 ? 3 : 1;
        int left = col % 3 == 0 ? 3 : 1;
        int bottom = row == BOARD_SIZE - 1 ? 3 : 1;
        int right = col == BOARD_SIZE - 1 ? 3 : 1;
        if (controller.isSelectedCell(row, col)) {
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SELECTED_BORDER, 4),
                    BorderFactory.createMatteBorder(top, left, bottom, right, GRID_BORDER));
        }
        return BorderFactory.createMatteBorder(top, left, bottom, right, GRID_BORDER);
    }
}
