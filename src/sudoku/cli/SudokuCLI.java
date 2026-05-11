package sudoku.cli;

import sudoku.model.ISudokuModel;

import java.util.Scanner;

// Command line version of the Sudoku game. It uses the same model as the GUI.
public class SudokuCLI {
    private static final int BOARD_SIZE = 9;
    private static final int EMPTY_VALUE = 0;

    private final ISudokuModel model;
    private final Scanner scanner;

    /**
     * Creates the CLI game.
     *
     * @param model the model that stores game state and rules
     */
    public SudokuCLI(ISudokuModel model) {
        assert model != null : "model must not be null";
        this.model = model;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the command line interaction.
     */
    public void run() {
        printHelp();
        boolean running = true;
        while (running) {
            printBoard();
            System.out.print("sudoku> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                running = handleCommand(input);
            }
        }
    }

    private boolean handleCommand(String input) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            if ("set".equals(command)) {
                handleSet(parts);
            } else if ("clear".equals(command)) {
                handleClear(parts);
            } else if ("undo".equals(command)) {
                handleUndo(parts);
            } else if ("hint".equals(command)) {
                handleHint(parts);
            } else if ("reset".equals(command)) {
                handleReset(parts);
            } else if ("new".equals(command)) {
                handleNewGame(parts);
            } else if ("help".equals(command)) {
                printHelp();
            } else if ("exit".equals(command)) {
                return false;
            } else {
                System.out.println("Unknown command. Type help to see the commands.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }

        if (model.isComplete()) {
            System.out.println("Puzzle completed!");
        }
        return true;
    }

    private void handleSet(String[] parts) {
        requirePartCount(parts, 4, "Usage: set <row> <col> <value>");
        int row = parseCoordinate(parts[1]);
        int col = parseCoordinate(parts[2]);
        int value = parseValue(parts[3]);

        if (model.setValue(row, col, value)) {
            System.out.println("Cell updated.");
        } else {
            System.out.println("Could not set that cell.");
        }
    }

    private void handleClear(String[] parts) {
        requirePartCount(parts, 3, "Usage: clear <row> <col>");
        int row = parseCoordinate(parts[1]);
        int col = parseCoordinate(parts[2]);

        if (model.clearValue(row, col)) {
            System.out.println("Cell cleared.");
        } else {
            System.out.println("Could not clear that cell.");
        }
    }

    private void handleUndo(String[] parts) {
        requirePartCount(parts, 1, "Usage: undo");
        if (model.undo()) {
            System.out.println("Last move undone.");
        } else {
            System.out.println("There is no move to undo.");
        }
    }

    private void handleHint(String[] parts) {
        requirePartCount(parts, 3, "Usage: hint <row> <col>");
        int row = parseCoordinate(parts[1]);
        int col = parseCoordinate(parts[2]);

        if (model.revealHint(row, col)) {
            System.out.println("Hint filled.");
        } else {
            System.out.println("Could not reveal a hint for that cell.");
        }
    }

    private void handleReset(String[] parts) {
        requirePartCount(parts, 1, "Usage: reset");
        model.reset();
        System.out.println("Puzzle reset.");
    }

    private void handleNewGame(String[] parts) {
        requirePartCount(parts, 1, "Usage: new");
        model.newGame();
        System.out.println("New puzzle started.");
    }

    private void printBoard() {
        System.out.println();
        System.out.println("    1  2  3   4  5  6   7  8  9");
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (row % 3 == 0) {
                System.out.println("  +---------+---------+---------+");
            }
            System.out.print((row + 1) + " |");
            for (int col = 0; col < BOARD_SIZE; col++) {
                System.out.print(formatCell(row, col));
                if ((col + 1) % 3 == 0) {
                    System.out.print("|");
                }
            }
            System.out.println();
        }
        System.out.println("  +---------+---------+---------+");
        printInvalidCells();
    }

    private String formatCell(int row, int col) {
        int value = model.getValueAt(row, col);
        if (value == EMPTY_VALUE) {
            return " . ";
        }
        if (model.isPreFilled(row, col)) {
            return "[" + value + "]";
        }
        return " " + value + " ";
    }

    private void printInvalidCells() {
        if (!model.isValidationFeedbackEnabled()) {
            return;
        }

        boolean foundInvalidCell = false;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (model.isCellInvalid(row, col)) {
                    if (!foundInvalidCell) {
                        System.out.print("Invalid cells: ");
                        foundInvalidCell = true;
                    }
                    System.out.print("(" + (row + 1) + "," + (col + 1) + ") ");
                }
            }
        }
        if (foundInvalidCell) {
            System.out.println();
        }
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  set <row> <col> <value>  Set a cell value");
        System.out.println("  clear <row> <col>        Clear a cell");
        System.out.println("  undo                     Undo the last move");
        System.out.println("  hint <row> <col>         Fill a hint");
        System.out.println("  reset                    Reset the current puzzle");
        System.out.println("  new                      Start a new puzzle");
        System.out.println("  help                     Show this help");
        System.out.println("  exit                     Exit the game");
        System.out.println("Rows and columns use 1-9.");
    }

    private int parseCoordinate(String text) {
        int value = parseInteger(text, "Coordinate must be a number from 1 to 9.");
        if (value < 1 || value > BOARD_SIZE) {
            throw new IllegalArgumentException("Coordinate must be from 1 to 9.");
        }
        return value - 1;
    }

    private int parseValue(String text) {
        int value = parseInteger(text, "Value must be a number from 1 to 9.");
        if (value < 1 || value > BOARD_SIZE) {
            throw new IllegalArgumentException("Value must be from 1 to 9.");
        }
        return value;
    }

    private int parseInteger(String text, String errorMessage) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void requirePartCount(String[] parts, int expectedCount, String usage) {
        if (parts.length != expectedCount) {
            throw new IllegalArgumentException(usage);
        }
    }
}
