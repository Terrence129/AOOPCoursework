package sudoku.gui;

import sudoku.model.ISudokuModel;
import sudoku.model.SudokuModel;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.nio.file.Paths;

// Starts the Swing version of the Sudoku game.
public class SudokuGuiMain {
    private static final Path PUZZLE_FILE = Paths.get("resources", "puzzles.txt");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public static void createAndShowGUI() {
        ISudokuModel model = new SudokuModel(PUZZLE_FILE);
        SudokuController controller = new SudokuController(model);
        SudokuView view = new SudokuView(model, controller);
    }
}
