package spinner.minesweeper;

import basicneuralnetwork.NeuralNetwork;

import java.io.IOException;
import java.util.Random;

public class MinesweeperNeuralNetwork {
    private NeuralNetwork network;
    private static final int BOARD_SIZE = 5;
    private static final int BOMBS = 3;
    private static final int INPUT_SIZE = BOARD_SIZE * BOARD_SIZE;
    private static final int OUTPUT_SIZE = BOARD_SIZE * BOARD_SIZE;
    private static final int HIDDEN_SIZE = 128;
    private static final double FLAG_THRESHOLD = 0.9;

    public void train(int numGames) {
        // 1. NeuralNetwork.readFromFile() or create new
        try {
            network = NeuralNetwork.readFromFile("nn_data.json");
            System.out.println("Loaded existing network");
        } catch (Exception e) {
            network = new NeuralNetwork(INPUT_SIZE, HIDDEN_SIZE, OUTPUT_SIZE);
            System.out.println("Created new network");
        }

        // 11. Do 1,000,000 times
        for (int game = 0; game < numGames; game++) {
            playTrainingGame();

            // Test every 10,000 games to show progress
            if ((game + 1) % 10000 == 0) {
                int wins = 0;
                for (int i = 0; i < 1000; i++) {
                    if (playTestGame()) {
                        wins++;
                    }
                }
                double accuracy = (double) wins / 1000;
                System.out.println("accuracy/wins/games/total_games = "
                        + String.format("%.4f", accuracy) + "/"
                        + wins + "/"
                        + 1000 + "/"
                        + (game + 1));
            }
        }

        // 12. NeuralNetwork.writeToFile()
        try {
            network.writeToFile("nn_data.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Training complete");
    }

    private void playTrainingGame() {
        // 2. Create Minesweeper game (5x5 with 3 bombs)
        Minesweeper game = new Minesweeper(BOARD_SIZE, BOMBS);

        // 3. Make an initial random move
        makeRandomMove(game);

        // 10. Loop until game won or game over
        while (game.getGameState() == Minesweeper.GameState.PLAYING) {
            // 4. Create a deep copy
            Minesweeper copy = game.deepCopy();

            // 5. AutoFlag on the copy
            copy.autoFlag();

            // 6. Get two arrays
            double[] input = game.toInput();
            double[] output = copy.toOutput();

            // 7. network.train(input, output)
            network.train(input, output);

            // 8. original = copy
            game = copy;

            // 9. If a flag was added, AutoReveal, otherwise reveal random cell
            boolean flagAdded = checkIfFlagAdded(input, output);
            if (flagAdded) {
                game.autoReveal();
            } else {
                makeRandomMove(game);
            }
        }
    }

    private boolean playTestGame() {
        Minesweeper game = new Minesweeper(BOARD_SIZE, BOMBS);
        makeRandomMove(game);

        while (game.getGameState() == Minesweeper.GameState.PLAYING) {
            double[] input = game.toInput();
            double[] output = network.guess(input);

            boolean flagAdded = false;
            for (int i = 0; i < output.length; i++) {
                if (output[i] >= FLAG_THRESHOLD) {
                    int row = i / BOARD_SIZE;
                    int col = i % BOARD_SIZE;
                    if (!game.isRevealed(row, col) && !game.isFlagged(row, col)) {
                        game.flagCell(row, col);
                        flagAdded = true;
                    }
                }
            }

            if (flagAdded) {
                game.autoReveal();
            } else {
                makeRandomMove(game);
            }
        }

        return game.getGameState() == Minesweeper.GameState.WON;
    }

    private void makeRandomMove(Minesweeper game) {
        Random random = new Random();
        java.util.List<int[]> availableCells = new java.util.ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!game.isRevealed(row, col) && !game.isFlagged(row, col)) {
                    availableCells.add(new int[]{row, col});
                }
            }
        }

        //if there are available cells, pick one randomly
        if (!availableCells.isEmpty()) {
            int randomIndex = random.nextInt(availableCells.size());
            int[] cell = availableCells.get(randomIndex);
            game.revealCell(cell[0], cell[1]);
        }
    }

    private boolean checkIfFlagAdded(double[] input, double[] output) {
        for (int i = 0; i < input.length; i++) {
            if (output[i] == 1.0 && input[i] != 1.0) {
                return true;
            }
        }
        return false;
    }

    public void test(int numGames) {
        // Load the trained network
        try {
            network = NeuralNetwork.readFromFile("nn_data.json");
            System.out.println("Loaded network for testing");
        } catch (Exception e) {
            System.out.println("No trained network found!");
            return;
        }

        int wins = 0;
        for (int game = 0; game < numGames; game++) {
            if (playTestGame()) {
                wins++;
            }
        }

        double winRate = (double) wins / numGames * 100;
        System.out.println("\n=== Test Results ===");
        System.out.println("Total Games: " + numGames);
        System.out.println("Wins: " + wins);
        System.out.println("Win Rate: " + String.format("%.2f", winRate) + "%");
        System.out.println("Expected: > 50%");
    }

    public static void main(String[] args) {
        MinesweeperNeuralNetwork trainer = new MinesweeperNeuralNetwork();
        System.out.println("Starting training...");
        trainer.train(100000);
    }
}