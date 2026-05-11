package sudoku.cli;

import sudoku.model.ISudokuModel;
import sudoku.model.SudokuModel;

import java.nio.file.Path;
import java.nio.file.Paths;

// Starts the command line version of the Sudoku game.
public class SudokuCliMain {
    private static final Path PUZZLE_FILE = Paths.get("resources", "puzzles.txt");

    public static void main(String[] args) {
        ISudokuModel model = new SudokuModel(PUZZLE_FILE);
        new SudokuCLI(model).run();
    }
}
