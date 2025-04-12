import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class SecretOfTheMermaid extends JFrame {
    private static final int ROWS = 5;
    private static final int COLS = 6;
    private static final String[] SYMBOLS = {
        "🐟", // Fish
        "🦀", // Crab
        "🐬", // Dolphin
        "🐚", // Shell
        "⚓", // Anchor
        "🔱", // Trident
        "💎", // Gem
        "🚢", // Ship
        "💰"  // Treasure
    };
    private static final double[] SYMBOL_PROBABILITIES = {
        0.15,  // Fish (common)
        0.14,  // Crab
        0.13,  // Dolphin
        0.12,  // Shell
        0.11,  // Anchor
        0.10,  // Trident
        0.09,  // Gem
        0.08,  // Ship
        0.04   // Treasure (rare)
    };
    
    private static final double[][] PAYOUT_MULTIPLIERS = {
        {0.25, 0.75, 2.00},    // Fish
        {0.40, 0.90, 4.00},    // Crab
        {0.50, 1.00, 5.00},    // Dolphin
        {0.80, 1.20, 8.00},    // Shell
        {1.00, 1.50, 10.00},   // Anchor
        {1.50, 2.00, 12.00},   // Trident
        {2.00, 5.00, 15.00},   // Gem
        {2.50, 10.00, 25.00},  // Ship
        {10.00, 25.00, 50.00}  // Treasure
    };
    
    private JLabel[][] gridLabels;
    private JLabel balanceLabel;
    private JLabel betMultiplierLabel;
    private JLabel coinValueLabel;
    private JLabel totalBetLabel;
    private int betMultiplier = 1;
    private double coinValue = 0.05;
    private double totalBet = betMultiplier * coinValue;
    private double balance = 1000.0;
    private Random random = new Random();
    private double mermaidChance = 0.1; // Starting 10% chance for mermaid multiplier
    private JLabel multiplierLabel;
    private boolean isSpinning = false;
    
    public SecretOfTheMermaid() {
        setTitle("Secret of the Mermaid");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS, 5, 5));
        gridLabels = new JLabel[ROWS][COLS];
        
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                gridLabels[i][j] = new JLabel("🌊", SwingConstants.CENTER);
                gridLabels[i][j].setFont(new Font("Dialog", Font.PLAIN, 30));
                gridPanel.add(gridLabels[i][j]);
            }
        }
        
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        balanceLabel = new JLabel(String.format("Balance: $%.2f", balance));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        controlPanel.add(balanceLabel, gbc);
        
        JLabel betMultiplierTitle = new JLabel("BET MULTIPLIER");
        gbc.gridy = 1;
        controlPanel.add(betMultiplierTitle, gbc);
        
        JButton decreaseBetMultiplier = new JButton("-");
        betMultiplierLabel = new JLabel(String.format("%dx", betMultiplier));
        JButton increaseBetMultiplier = new JButton("+");
        
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        controlPanel.add(decreaseBetMultiplier, gbc);
        gbc.gridx = 1;
        controlPanel.add(betMultiplierLabel, gbc);
        gbc.gridx = 2;
        controlPanel.add(increaseBetMultiplier, gbc);
        
        JLabel coinValueTitle = new JLabel("COIN VALUE");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        controlPanel.add(coinValueTitle, gbc);
        
        JButton decreaseCoinValue = new JButton("-");
        coinValueLabel = new JLabel(String.format("$%.2f", coinValue));
        JButton increaseCoinValue = new JButton("+");
        
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        controlPanel.add(decreaseCoinValue, gbc);
        gbc.gridx = 1;
        controlPanel.add(coinValueLabel, gbc);
        gbc.gridx = 2;
        controlPanel.add(increaseCoinValue, gbc);
        
        JLabel totalBetTitle = new JLabel("TOTAL BET");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        controlPanel.add(totalBetTitle, gbc);
        
        totalBetLabel = new JLabel(String.format("$%.2f", totalBet));
        gbc.gridy = 6;
        controlPanel.add(totalBetLabel, gbc);
        
        JButton spinButton = new JButton("SPIN");
        JButton maxBetButton = new JButton("BET MAX");
        
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        controlPanel.add(spinButton, gbc);
        gbc.gridx = 2;
        controlPanel.add(maxBetButton, gbc);
        
        multiplierLabel = new JLabel("Mermaid Chance: 10%");
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        controlPanel.add(multiplierLabel, gbc);
        
        decreaseBetMultiplier.addActionListener(_ -> {
            if (betMultiplier > 1) {
                betMultiplier--;
                updateBetLabels();
            }
        });
        
        increaseBetMultiplier.addActionListener(_ -> {
            if (betMultiplier < 20) {
                betMultiplier++;
                updateBetLabels();
            }
        });
        
        decreaseCoinValue.addActionListener(_ -> {
            if (coinValue > 0.05) {
                coinValue = Math.round((coinValue - 0.05) * 100.0) / 100.0;
                updateBetLabels();
            }
        });
        
        increaseCoinValue.addActionListener(_ -> {
            if (coinValue < 1.00) {
                coinValue = Math.round((coinValue + 0.05) * 100.0) / 100.0;
                updateBetLabels();
            }
        });
        
        maxBetButton.addActionListener(_ -> {
            betMultiplier = 20;
            coinValue = 1.00;
            updateBetLabels();
        });
        
        spinButton.addActionListener(_ -> {
            if (!isSpinning) {
                spin();
            }
        });
        
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        setSize(400, 800);
        setLocationRelativeTo(null);
    }
    
    private void updateBetLabels() {
        totalBet = betMultiplier * coinValue;
        betMultiplierLabel.setText(String.format("%dx", betMultiplier));
        coinValueLabel.setText(String.format("$%.2f", coinValue));
        totalBetLabel.setText(String.format("$%.2f", totalBet));
    }
    
    private void spin() {
        if (totalBet <= 0 || totalBet > balance) {
            JOptionPane.showMessageDialog(this, "Invalid bet amount!");
            return;
        }
        
        isSpinning = true;
        balance -= totalBet;
        updateBalance();
        
        String[][] grid = new String[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = getRandomSymbol();
            }
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                for (int k = 0; k < 20; k++) {
                    for (int i = 0; i < ROWS; i++) {
                        for (int j = 0; j < COLS; j++) {
                            final int row = i;
                            final int col = j;
                            SwingUtilities.invokeLater(() -> 
                                gridLabels[row][col].setText(getRandomSymbol())
                            );
                        }
                    }
                    Thread.sleep(50);
                }
                
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        final int row = i;
                        final int col = j;
                        final String symbol = grid[row][col];
                        SwingUtilities.invokeLater(() -> 
                            gridLabels[row][col].setText(symbol)
                        );
                    }
                }
                
                checkConsecutiveWins(grid, totalBet);
                
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                isSpinning = false;
            }
        });
    }
    
    private void checkConsecutiveWins(String[][] grid, double bet) {
        calculateWins(grid, bet, new StringBuilder(), 0.0, 1);
    }
    
    private void calculateWins(String[][] grid, double bet, 
            StringBuilder totalWinMessage, double totalWinsSoFar, int consecutiveWinCount) {
        int[] symbolCounts = new int[SYMBOLS.length];
        boolean[][] matchedPositions = new boolean[ROWS][COLS];
        boolean hasWin = false;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                String symbol = grid[i][j];
                for (int k = 0; k < SYMBOLS.length; k++) {
                    if (symbol.equals(SYMBOLS[k])) {
                        symbolCounts[k]++;
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < SYMBOLS.length; i++) {
            if (symbolCounts[i] >= 8) {
                hasWin = true;
                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        if (grid[row][col].equals(SYMBOLS[i])) {
                            matchedPositions[row][col] = true;
                        }
                    }
                }
            }
        }

        if (!hasWin) {
            if (consecutiveWinCount > 1) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        String.format("%s\nTotal Consecutive Wins: %d\nTotal Win: $%.2f!", 
                            totalWinMessage.toString(), consecutiveWinCount - 1, totalWinsSoFar));
                });
            }
            mermaidChance += 0.05; // Increase chance by 5% on loss
            updateMultiplier(); // Update label after increasing chance
            return;
        }

        double currentRoundWin = 0.0;
        StringBuilder winMessage = new StringBuilder();
        if (consecutiveWinCount > 1) {
            winMessage.append(String.format("Consecutive Win #%d!\n\n", consecutiveWinCount));
        }
        winMessage.append("Wins:\n");

        for (int i = 0; i < SYMBOLS.length; i++) {
            int count = symbolCounts[i];
            if (count >= 8) {
                double multiplier;
                if (count <= 9) multiplier = PAYOUT_MULTIPLIERS[i][0];
                else if (count <= 11) multiplier = PAYOUT_MULTIPLIERS[i][1];
                else multiplier = PAYOUT_MULTIPLIERS[i][2];

                double win = bet * multiplier;
                currentRoundWin += win;
                winMessage.append(String.format("%s x%d: $%.2f\n", SYMBOLS[i], count, win));
            }
        }

        if (random.nextDouble() < mermaidChance) {
            double mermaidMultiplier = (random.nextDouble() * 5) + 2;
            currentRoundWin *= mermaidMultiplier;
            winMessage.append(String.format("\nMermaid Multiplier: x%.2f!", mermaidMultiplier));
            mermaidChance = 0.1; // Reset chance after getting mermaid multiplier
            updateMultiplier(); // Update label after resetting chance
        } else if (consecutiveWinCount > 1) {
            mermaidChance += 0.1; // Increase chance by 10% on consecutive win without mermaid
            updateMultiplier(); // Update label after increasing chance
        }

        balance += currentRoundWin;
        double newTotalWins = totalWinsSoFar + currentRoundWin;

        if (totalWinMessage.length() > 0) {
            totalWinMessage.append("\n\n");
        }
        totalWinMessage.append(winMessage);

        SwingUtilities.invokeLater(() -> {
            updateBalance();
            animateMatchesAndCascade(matchedPositions, totalWinMessage.toString(), 
                newTotalWins, grid, bet, totalWinMessage, newTotalWins, consecutiveWinCount + 1);
        });
    }
    
    private void animateMatchesAndCascade(boolean[][] matchedPositions, String winMessage, 
            double totalWin, String[][] originalGrid, double bet,
            StringBuilder totalWinMessage, double totalWins, int nextConsecutiveWinCount) {
        CompletableFuture.runAsync(() -> {
            try {
                for (int scale = 130; scale >= 70; scale -= 10) {
                    final int finalScale = scale;
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < ROWS; i++) {
                            for (int j = 0; j < COLS; j++) {
                                if (matchedPositions[i][j]) {
                                    Font currentFont = gridLabels[i][j].getFont();
                                    gridLabels[i][j].setFont(
                                        currentFont.deriveFont((float)(currentFont.getSize() * finalScale / 100))
                                    );
                                }
                            }
                        }
                    });
                    Thread.sleep(50);
                }

                String[][] newGrid = new String[ROWS][COLS];
                for (int i = 0; i < ROWS; i++) {
                    System.arraycopy(originalGrid[i], 0, newGrid[i], 0, COLS);
                }

                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        if (matchedPositions[i][j]) {
                            newGrid[i][j] = "";
                            final int row = i;
                            final int col = j;
                            SwingUtilities.invokeLater(() -> {
                                gridLabels[row][col].setText("");
                                gridLabels[row][col].setFont(new Font("Dialog", Font.PLAIN, 30));
                            });
                        }
                    }
                }
                Thread.sleep(300);

                for (int col = 0; col < COLS; col++) {
                    int emptyRow = ROWS - 1;
                    for (int row = ROWS - 1; row >= 0; row--) {
                        if (!newGrid[row][col].isEmpty()) {
                            if (row != emptyRow) {
                                newGrid[emptyRow][col] = newGrid[row][col];
                                newGrid[row][col] = "";
                            }
                            emptyRow--;
                        }
                    }
                    for (int row = emptyRow; row >= 0; row--) {
                        newGrid[row][col] = getRandomSymbol();
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < ROWS; i++) {
                        for (int j = 0; j < COLS; j++) {
                            gridLabels[i][j].setText(newGrid[i][j]);
                        }
                    }
                });
                Thread.sleep(500);

                calculateWins(newGrid, bet, totalWinMessage, totalWins, nextConsecutiveWinCount);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
    
    private String getRandomSymbol() {
        double value = random.nextDouble();
        double cumulative = 0.0;
        
        for (int i = 0; i < SYMBOL_PROBABILITIES.length; i++) {
            cumulative += SYMBOL_PROBABILITIES[i];
            if (value <= cumulative) {
                return SYMBOLS[i];
            }
        }
        
        return SYMBOLS[SYMBOLS.length - 1];
    }
    
    private void updateBalance() {
        SwingUtilities.invokeLater(() -> 
            balanceLabel.setText(String.format("Balance: $%.2f", balance))
        );
    }
    
    private void updateMultiplier() {
        SwingUtilities.invokeLater(() -> 
            multiplierLabel.setText(String.format("Mermaid Chance: %.0f%%", mermaidChance * 100))
        );
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SecretOfTheMermaid().setVisible(true);
        });
    }
}