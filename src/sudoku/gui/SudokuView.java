package sudoku.gui;

import sudoku.model.SudokuModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

// Shows the Sudoku GUI and refreshes itself when the model changes.
public class SudokuView implements Observer {
    private static final int BOARD_SIZE = 9;
    private static final int EMPTY_VALUE = 0;
    private static final Color NORMAL_BACKGROUND = Color.WHITE;
    private static final Color PREFILLED_BACKGROUND = new Color(230, 230, 230);
    private static final Color INVALID_BACKGROUND = new Color(255, 180, 180);

    private final SudokuModel model;
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
    public SudokuView(SudokuModel model, SudokuController controller) {
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

        buildFrame();
        model.addObserver(this);
        update(model, null);
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
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyChar() >= '1' && event.getKeyChar() <= '9') {
                    controller.onNumberInput(event.getKeyChar() - '0');
                }
            }
        });
        frame.add(createCheckBoxPanel(), BorderLayout.NORTH);
        frame.add(createBoardPanel(), BorderLayout.CENTER);
        frame.add(createButtonPanel(), BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createCheckBoxPanel() {
        JPanel panel = new JPanel();
        validationFeedbackCheckBox.addActionListener(event -> {
            if (!updatingView) {
                controller.onValidationFeedbackChanged(validationFeedbackCheckBox.isSelected());
            }
        });
        hintEnabledCheckBox.addActionListener(event -> {
            if (!updatingView) {
                controller.onHintEnabledChanged(hintEnabledCheckBox.isSelected());
            }
        });
        randomPuzzleCheckBox.addActionListener(event -> {
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
        button.setFocusPainted(false);
        button.setBorder(createCellBorder(row, col));
        button.addActionListener(event -> controller.onCellClicked(row, col));
        return button;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        eraseButton.addActionListener(event -> controller.onEraseClicked());
        undoButton.addActionListener(event -> controller.onUndoClicked());
        hintButton.addActionListener(event -> controller.onHintClicked());
        resetButton.addActionListener(event -> controller.onResetClicked());
        newGameButton.addActionListener(event -> controller.onNewGameClicked());

        panel.add(eraseButton);
        panel.add(undoButton);
        panel.add(hintButton);
        panel.add(resetButton);
        panel.add(newGameButton);
        return panel;
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
        button.setForeground(Color.BLACK);
        button.setFont(new Font(Font.SANS_SERIF, preFilled ? Font.BOLD : Font.PLAIN, 22));
        if (model.isCellInvalid(row, col)) {
            button.setBackground(INVALID_BACKGROUND);
        } else if (preFilled) {
            button.setBackground(PREFILLED_BACKGROUND);
        } else {
            button.setBackground(NORMAL_BACKGROUND);
        }
        button.setOpaque(true);
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
        return BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK);
    }
}
