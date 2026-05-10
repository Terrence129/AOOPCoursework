package sudoku.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

// Starts the command line version of the Sudoku game.
public final class SudokuCliMain {
    private static final Path PUZZLE_FILE = Paths.get("resources", "puzzles.txt");

    private SudokuCliMain() {
        // Main class does not need objects.
    }

    /**
     * Starts the CLI program.
     *
     * @param args command line arguments, not used
     */
    public static void main(String[] args) {
        SudokuCLI cli = new SudokuCLI(PUZZLE_FILE);
        cli.run();
    }
}
