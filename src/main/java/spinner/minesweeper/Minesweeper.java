package spinner.minesweeper;

/*This is the model (game logic)
- Handles bomb placement, cell revealing, win/lose detection
- No UI code - AI will be using this code
*/

import java.util.Random;

public class Minesweeper
{
    private static final int BOARD_SIZE = 9;
    private static final int NUM_BOMBS = 10;
    private boolean[][] bombs;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int flagCount;
    private GameState gameState;

    public enum GameState
    {
        PLAYING, WON, LOST;
    }

    public enum CellResult {
        BOMB, EMPTY, NUMBER, ALREADY_REVEALED, FLAGGED
    }

    public Minesweeper() {
        bombs = new boolean[BOARD_SIZE][BOARD_SIZE];
        revealed = new boolean[BOARD_SIZE][BOARD_SIZE];
        flagged = new boolean[BOARD_SIZE][BOARD_SIZE];

        gameState = GameState.PLAYING;
        flagCount = 0;

        placeBombs();
    }

    public void placeBombs() {
        Random random = new Random();
        int bombsPlaced = 0;

        while (bombsPlaced < NUM_BOMBS) {
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);

            if (!bombs[row][col]) {
                bombs[row][col] = true;
                bombsPlaced++;
            }
        }
    }

    public CellResult revealCell(int row, int col) {
        if (gameState != GameState.PLAYING) {
            return CellResult.ALREADY_REVEALED;
        }

        if (revealed[row][col]) {
            return CellResult.ALREADY_REVEALED;
        }

        if (flagged[row][col]) {
            return CellResult.FLAGGED;
        }

        revealed[row][col] = true;

        if (bombs[row][col]) {
            gameState = GameState.LOST;
            return CellResult.BOMB;
        }

        int adjacentBombs = countAdjacentBombs(row, col);
        if (adjacentBombs == 0) {
            revealNeighbors(row, col);
            checkWinCondition(); //did win?
            return CellResult.EMPTY;
        }

        checkWinCondition();
        return CellResult.NUMBER;
    }

    public void revealAllCells() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                revealed[row][col] = true;
            }
        }
    }

    public int countAdjacentBombs(int row, int col) {
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                // Calculate the neighbor's position
                int newRow = row + dr;
                int newCol = col + dc;

                // Check if neighbor is within bounds and has a bomb
                if (isValid(newRow, newCol) && bombs[newRow][newCol]) {
                    count++;
                }
            }
        }

        return count;
    }

    private void revealNeighbors(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int newRow = row + dr;
                int newCol = col + dc;

                if (isValid(newRow, newCol) && !revealed[newRow][newCol] && !flagged[newRow][newCol]) {
                    revealed[newRow][newCol] = true;

                    if (countAdjacentBombs(newRow, newCol) == 0) {
                        revealNeighbors(newRow, newCol);
                    }
                }
            }
        }
    }

    public void flagCell(int row, int col) {
        if (gameState != GameState.PLAYING || revealed[row][col]) {
            return;
        }

        if (flagged[row][col]) {
            flagged[row][col] = false;
            flagCount--;
        } else {
            flagged[row][col] = true;
            flagCount++;
        }
    }

    private void checkWinCondition() {
        // Count how many non-bomb cells are revealed
        int revealedCount = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (revealed[row][col] && !bombs[row][col]) {
                    revealedCount++;
                }
            }
        }

        int totalNonBombs = BOARD_SIZE * BOARD_SIZE - NUM_BOMBS;

        if (revealedCount == totalNonBombs) {
            gameState = GameState.WON;
        }
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public boolean isRevealed(int row, int col) {
        return revealed[row][col];
    }

    public boolean isFlagged(int row, int col) {
        return flagged[row][col];
    }

    public boolean hasBomb(int row, int col) {
        return bombs[row][col];
    }

    public int getFlagCount() {
        return flagCount;
    }

    public int getTotalBombs() {
        return NUM_BOMBS;
    }

    public int[][] getVisibleBoard() {
        int[][] visible = new int[BOARD_SIZE][BOARD_SIZE];

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (flagged[row][col]) {
                    visible[row][col] = -1;
                } else if (!revealed[row][col]) {
                    visible[row][col] = -2;
                } else {
                    visible[row][col] = countAdjacentBombs(row, col);
                }
            }
        }

        return visible;
    }

    public void autoFlag() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Skip unrevealed, flagged, or bomb cells
                if (!revealed[row][col] || flagged[row][col] || bombs[row][col]) {
                    continue;
                }

                // For numbered cells
                int adjacentBombs = countAdjacentBombs(row, col);

                // Skip empty cells
                if (adjacentBombs == 0) {
                    continue;
                }

                // Count hidden and flagged neighbors
                int hiddenCount = countHiddenNeighbors(row, col);
                int flaggedCount = countFlaggedNeighbors(row, col);

                // Cell is "satisfied" if hidden + flagged == number of mines
                if (hiddenCount + flaggedCount == adjacentBombs) {
                    // All hidden neighbors must be bombs - flag them
                    flagHiddenNeighbors(row, col);
                }
            }
        }
    }

    public void autoReveal() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Skip unrevealed, flagged, or bomb cells
                if (!revealed[row][col] || flagged[row][col] || bombs[row][col]) {
                    continue;
                }

                int adjacentBombs = countAdjacentBombs(row, col);

                if (adjacentBombs == 0) {
                    continue;
                }

                int flaggedCount = countFlaggedNeighbors(row, col);

                if (flaggedCount == adjacentBombs) {
                    revealHiddenNeighbors(row, col);
                }
            }
        }
    }

    private int countHiddenNeighbors(int row, int col) {
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue; // Skip self
                }

                int newRow = row + dr;
                int newCol = col + dc;

                if (isValid(newRow, newCol)) {
                    // Hidden = not revealed AND not flagged
                    if (!revealed[newRow][newCol] && !flagged[newRow][newCol]) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    private int countFlaggedNeighbors(int row, int col) {
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int newRow = row + dr;
                int newCol = col + dc;

                if (isValid(newRow, newCol) && flagged[newRow][newCol]) {
                    count++;
                }
            }
        }

        return count;
    }

    private void flagHiddenNeighbors(int row, int col)
    {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int newRow = row + dr;
                int newCol = col + dc;

                if (isValid(newRow, newCol)) {
                    // Flag if hidden (not revealed and not already flagged)
                    if (!revealed[newRow][newCol] && !flagged[newRow][newCol]) {
                        flagged[newRow][newCol] = true;
                        flagCount++;
                    }
                }
            }
        }
    }

    private void revealHiddenNeighbors(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int newRow = row + dr;
                int newCol = col + dc;

                if (isValid(newRow, newCol)) {
                    // Reveal if hidden (not revealed and not flagged)
                    if (!revealed[newRow][newCol] && !flagged[newRow][newCol]) {
                        revealCell(newRow, newCol);
                    }
                }
            }
        }
    }

    public double[] toInput() {
        int size = BOARD_SIZE * BOARD_SIZE;
        double[] input = new double[size];

        int index = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (flagged[row][col]) {
                    input[index] = 1.0;
                } else if (revealed[row][col]) {
                    int num = countAdjacentBombs(row, col);
                    input[index] = (num == 0) ? 0.1 : num * 0.1;
                } else {
                    // Hidden cell
                    input[index] = 0.0;
                }
                index++;
            }
        }

        return input;
    }

    public double[] toOutput() {
        int size = BOARD_SIZE * BOARD_SIZE; // 81
        double[] output = new double[size];

        int index = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                output[index] = flagged[row][col] ? 1.0 : 0.0;
                index++;
            }
        }

        return output;
    }

    public Minesweeper deepCopy() {
        Minesweeper copy = new Minesweeper();

        // Copy arrays using streams
        copy.bombs = java.util.Arrays.stream(this.bombs)
                .map(boolean[]::clone)
                .toArray(boolean[][]::new);

        copy.revealed = java.util.Arrays.stream(this.revealed)
                .map(boolean[]::clone)
                .toArray(boolean[][]::new);

        copy.flagged = java.util.Arrays.stream(this.flagged)
                .map(boolean[]::clone)
                .toArray(boolean[][]::new);

        // Copy simple fields
        copy.flagCount = this.flagCount;
        copy.gameState = this.gameState;

        return copy;
    }
}