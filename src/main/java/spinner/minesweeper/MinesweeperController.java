package spinner.minesweeper;

import java.util.Random;

public class MinesweeperController
{
    private Minesweeper model;
    private MinesweeperFrame view;

    public MinesweeperController(MinesweeperFrame view) {
        this.view = view;
        this.model = new Minesweeper();
    }

    public void handleCellReveal(int row, int col) {
        Minesweeper.CellResult result = model.revealCell(row, col);

        if (result == Minesweeper.CellResult.BOMB) {
            view.stopTimer();
            model.revealAllCells();
            view.updateBoard();
            view.revealAllBombs();
            view.showGameOver();
        } else if (result == Minesweeper.CellResult.EMPTY || result == Minesweeper.CellResult.NUMBER) {
            view.updateBoard();

            if (model.getGameState() == Minesweeper.GameState.WON) {
                view.stopTimer();
                view.showWin();
            }
        }
    }

    public void handleCellFlag(int row, int col) {
        if (model.getGameState() != Minesweeper.GameState.PLAYING) {
            return;
        }

        if (model.isRevealed(row, col)) {
            return;
        }

        model.flagCell(row, col);

        view.updateCell(row, col);
        view.updateFlagsLabel();
    }

    public void handleNewGame() {
        view.stopTimer();

        model = new Minesweeper();

        // Resetting view
        view.resetBoard();
        view.updateFlagsLabel();
        view.resetTimer();
        view.startTimer();
    }

    public Minesweeper getModel() {
        return model;
    }

    public void handleAutoFlag() {
        if (model.getGameState() != Minesweeper.GameState.PLAYING) {
            return;
        }

        model.autoFlag();
        view.updateBoard();
        view.updateFlagsLabel();
    }

    public void handleAutoReveal() {
        if (model.getGameState() != Minesweeper.GameState.PLAYING) {
            return;
        }

        model.autoReveal();
        view.updateBoard();

        if (model.getGameState() == Minesweeper.GameState.WON) {
            view.stopTimer();
            view.showWin();
        } else if (model.getGameState() == Minesweeper.GameState.LOST) {
            view.stopTimer();
            model.revealAllCells();
            view.updateBoard();
            view.revealAllBombs();
            view.showGameOver();
        }
    }
}
