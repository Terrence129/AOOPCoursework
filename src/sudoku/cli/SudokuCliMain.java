package sudoku.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

// Starts the command line version of the Sudoku game.
public class SudokuCliMain {
    private static final Path PUZZLE_FILE = Paths.get("resources", "puzzles.txt");

    /**
     * Starts the CLI program.
     *
     * @param args command line arguments, not used
     */
    public static void main(String[] args) {
        new SudokuCLI(PUZZLE_FILE).run();
    }
}
