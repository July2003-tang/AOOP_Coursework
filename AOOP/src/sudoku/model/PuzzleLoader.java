package sudoku.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PuzzleLoader {

    // standard Sudoku size
    private static final int SIZE = 9;

    // private constructor: this is a utility class (no objects needed)
    private PuzzleLoader() {
    }

    // load all puzzles from a file
    public static List<Board> loadPuzzles(String filename) {
        List<Board> puzzles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            List<String> currentPuzzleLines = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // empty line means end of one puzzle
                if (line.isEmpty()) {
                    if (!currentPuzzleLines.isEmpty()) {
                        puzzles.add(createBoardFromLines(currentPuzzleLines));
                        currentPuzzleLines.clear();
                    }
                } else {
                    currentPuzzleLines.add(line);
                }
            }

            // handle last puzzle in file
            if (!currentPuzzleLines.isEmpty()) {
                puzzles.add(createBoardFromLines(currentPuzzleLines));
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read puzzle file: " + filename, e);
        }

        // make sure at least one puzzle exists
        if (puzzles.isEmpty()) {
            throw new IllegalArgumentException("No puzzles found in file: " + filename);
        }

        return puzzles;
    }

    // load only the first puzzle (used when random selection is off)
    public static Board loadFirstPuzzle(String filename) {
        return loadPuzzles(filename).get(0);
    }

    // convert 9 lines of text into a Board object
    private static Board createBoardFromLines(List<String> lines) {

        // each puzzle must have exactly 9 rows
        if (lines.size() != SIZE) {
            throw new IllegalArgumentException("Each puzzle must contain exactly 9 lines.");
        }

        int[][] values = new int[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            // remove spaces if any
            String line = lines.get(row).replaceAll("\\s+", "");

            // each row must have exactly 9 characters
            if (line.length() != SIZE) {
                throw new IllegalArgumentException("Each puzzle row must contain exactly 9 values.");
            }

            for (int col = 0; col < SIZE; col++) {
                char ch = line.charAt(col);

                // '.' or '0' means empty cell
                if (ch == '.' || ch == '0') {
                    values[row][col] = 0;
                }
                // digits 1-9
                else if (ch >= '1' && ch <= '9') {
                    values[row][col] = ch - '0';
                }
                // invalid character
                else {
                    throw new IllegalArgumentException("Invalid character in puzzle file: " + ch);
                }
            }
        }

        return new Board(values);
    }
}