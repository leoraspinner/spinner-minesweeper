package spinner.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MinesweeperFrame extends JFrame {
    private MinesweeperController controller;

    private JButton[][] cellButtons;
    private JLabel flagsLabel;
    private JButton newGameButton;
    private JLabel timerLabel;

    private Timer timer;
    private int elapsedSeconds;

    private static final Color BLUE_1 = new Color(0, 0, 255);        //"1"
    private static final Color GREEN_2 = new Color(0, 128, 0);       // "2"
    private static final Color RED_3 = new Color(255, 0, 0);         //"3"
    private static final Color DARK_BLUE_4 = new Color(0, 0, 128);   //"4"
    private static final Color MAROON_5 = new Color(128, 0, 0);      //"5"
    private static final Color CYAN_6 = new Color(0, 128, 128);      //"6"
    private static final Color BLACK_7 = Color.BLACK;                 //"7"
    private static final Color GRAY_8 = Color.GRAY;                   //"8"

    private static final Color UNREVEALED_CELL = new Color(240, 240, 240);  //unrevealed cell
    private static final Color REVEALED_CELL = new Color(150, 150, 150);     //revealed cell
    private static final Color FLAG_COLOR = Color.RED;
    private static final String BOMB_EMOJI = "💣";
    private static final String FLAG_EMOJI = "🚩";

    public MinesweeperFrame() {
        super("Minesweeper");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createBoardPanel(), BorderLayout.CENTER);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setController(MinesweeperController controller) {
        this.controller = controller;
    }

    private JPanel createTopPanel() {
        final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        flagsLabel = new JLabel("Flags: 10");
        flagsLabel.setFont(new Font("Arial", Font.BOLD, 16));

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.PLAIN, 14));
        newGameButton.addActionListener(e -> controller.handleNewGame());

        JButton autoFlagButton = new JButton("Auto Flag");
        autoFlagButton.setFont(new Font("Arial", Font.PLAIN, 14));
        autoFlagButton.addActionListener(e -> controller.handleAutoFlag());

        JButton autoRevealButton = new JButton("Auto Reveal");
        autoRevealButton.setFont(new Font("Arial", Font.PLAIN, 14));
        autoRevealButton.addActionListener(e -> controller.handleAutoReveal());

        timerLabel = new JLabel("Time: 000");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(flagsLabel);
        topPanel.add(newGameButton);
        topPanel.add(autoFlagButton);
        topPanel.add(autoRevealButton);
        topPanel.add(timerLabel);

        return topPanel;
    }

    private JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(9, 9, 0, 0));

        cellButtons = new JButton[9][9];

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                JButton cellButton = new JButton();

                cellButton.setPreferredSize(new Dimension(50, 50));
                cellButton.setBackground(UNREVEALED_CELL);
                cellButton.setFont(new Font("Arial", Font.BOLD, 20));
                cellButton.setBorder(BorderFactory.createRaisedBevelBorder());
                cellButton.setFocusPainted(false);
                cellButton.setOpaque(true);
                cellButton.setContentAreaFilled(false);
                cellButton.setBorderPainted(true);

                final int currentRow = row;
                final int currentCol = col;

                cellButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            controller.handleCellReveal(currentRow, currentCol);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            controller.handleCellFlag(currentRow, currentCol);
                        }
                    }
                });

                cellButtons[row][col] = cellButton;
                boardPanel.add(cellButton);
            }
        }
        return boardPanel;
    }

    public void updateBoard() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                updateCell(row, col);
            }
        }
    }

    public void updateCell(int row, int col) {
        JButton button = cellButtons[row][col];
        Minesweeper model = controller.getModel();

        if (model.isFlagged(row, col)) {
            displayFlaggedCell(button);
        } else if (model.isRevealed(row, col)) {
            displayRevealedCell(button, row, col);  // Pass row and col
        } else {
            displayUnrevealedCell(button);
        }
    }

    private void displayFlaggedCell(JButton button) {
        button.setText("🚩");
        button.setForeground(FLAG_COLOR);
        button.setBackground(UNREVEALED_CELL);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setEnabled(true);
    }

    private void displayRevealedCell(JButton button, int row, int col) {
        final Minesweeper model = controller.getModel();

        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setBackground(REVEALED_CELL);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        button.setEnabled(false);

        if (model.hasBomb(row, col)) {
            displayBombCell(button);
        } else {
            int adjacentBombs = model.countAdjacentBombs(row, col);
            if (adjacentBombs == 0) {
                displayEmptyRevealedCell(button);
            } else {
                displayNumberedCell(button, adjacentBombs);
            }
        }
    }

    private void displayBombCell(JButton button) {
        button.removeAll();
        button.setText("💣");
        button.setForeground(Color.BLACK);
        button.revalidate();
        button.repaint();
    }

    private void displayEmptyRevealedCell(JButton button) {
        button.removeAll();
        button.setText("");
        button.revalidate();
        button.repaint();
    }

    private void displayNumberedCell(JButton button, int adjacentBombs) {
        button.setText("");
        button.removeAll();
        button.setLayout(new BorderLayout());

        JLabel label = new JLabel(String.valueOf(adjacentBombs), SwingConstants.CENTER);
        label.setFont(button.getFont());
        label.setForeground(getColorForNumber(adjacentBombs));

        button.add(label, BorderLayout.CENTER);
        button.revalidate();
        button.repaint();
    }

    private Color getColorForNumber(int number) {
        switch (number) {
            case 1:
                return BLUE_1;
            case 2:
                return GREEN_2;
            case 3:
                return RED_3;
            case 4:
                return DARK_BLUE_4;
            case 5:
                return MAROON_5;
            case 6:
                return CYAN_6;
            case 7:
                return BLACK_7;
            case 8:
                return GRAY_8;
            default:
                return Color.BLACK;
        }
    }

    private void displayUnrevealedCell(JButton button) {
        button.setText("");
        button.setBackground(UNREVEALED_CELL);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setEnabled(true);
    }


    public void revealAllBombs() {
        Minesweeper model = controller.getModel();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (model.hasBomb(row, col)) {
                    cellButtons[row][col].setText("💣");
                    cellButtons[row][col].setForeground(FLAG_COLOR);
                    cellButtons[row][col].setBackground(REVEALED_CELL);
                    cellButtons[row][col].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    cellButtons[row][col].setEnabled(false);
                } else {
                    updateCell(row, col);
                }
            }
        }
    }

    public void updateFlagsLabel() {
        Minesweeper model = controller.getModel();

        int remainingFlags = model.getTotalBombs() - model.getFlagCount();
        flagsLabel.setText("Flags: " + remainingFlags);
    }

    public void resetBoard() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                JButton button = cellButtons[row][col];
                button.removeAll();
                button.setText("");
                button.setLayout(new BorderLayout());
                button.setBackground(UNREVEALED_CELL);
                button.setBorder(BorderFactory.createRaisedBevelBorder());
                button.setEnabled(true);
                button.revalidate();
                button.repaint();
            }
        }
    }

    public void startTimer() {
        elapsedSeconds = 0;

        timer = new Timer(1000, e -> {
            elapsedSeconds++;

            timerLabel.setText(String.format("Time: %03d", elapsedSeconds));
        });

        timer.start();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void resetTimer() {
        elapsedSeconds = 0;
        timerLabel.setText("Time: 000");
    }

    public void showGameOver() {
        JOptionPane.showMessageDialog(this, "Game Over! You hit a bomb.");
    }

    public void showWin() {
        JOptionPane.showMessageDialog(this, "You Won!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MinesweeperFrame view = new MinesweeperFrame();
            MinesweeperController controller = new MinesweeperController(view);
            view.setController(controller);
            view.updateFlagsLabel();
            view.startTimer();
        });
    }
}
