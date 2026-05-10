package sudoku.gui;

import sudoku.model.SudokuModel;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.nio.file.Paths;

// Starts the Swing version of the Sudoku game.
public final class SudokuGuiMain {
    private static final Path PUZZLE_FILE = Paths.get("resources", "puzzles.txt");

    private SudokuGuiMain() {
        // Main class does not need objects.
    }

    /**
     * Starts the GUI program.
     *
     * @param args command line arguments, not used
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SudokuModel model = new SudokuModel(PUZZLE_FILE);
                SudokuController controller = new SudokuController(model);
                SudokuView view = new SudokuView(model, controller);
                controller.setView(view);
            }
        });
    }
}
