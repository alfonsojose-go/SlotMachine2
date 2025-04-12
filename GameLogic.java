import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;

public class GameLogic {
    private static final int ROWS = 5;
    private static final int COLS = 6;
    private static final Symbol[] SYMBOLS = {
        new Symbol("🐟", "Fish", 0.15, new double[]{0.25, 0.75, 2.00}),
        new Symbol("🦀", "Crab", 0.14, new double[]{0.40, 0.90, 4.00}),
        new Symbol("🐬", "Dolphin", 0.13, new double[]{0.50, 1.00, 5.00}),
        new Symbol("🐚", "Shell", 0.12, new double[]{0.80, 1.20, 8.00}),
        new Symbol("⚓", "Anchor", 0.11, new double[]{1.00, 1.50, 10.00}),
        new Symbol("🔱", "Trident", 0.10, new double[]{1.50, 2.00, 12.00}),
        new Symbol("💎", "Gem", 0.09, new double[]{2.00, 5.00, 15.00}),
        new Symbol("🚢", "Ship", 0.08, new double[]{2.50, 10.00, 25.00}),
        new Symbol("💰", "Treasure", 0.04, new double[]{10.00, 25.00, 50.00})
    };

    private Random random;
    private GameState gameState;
    private SlotMachine slotMachine;
    private double mermaidChance = 0.1; // Starting 10% chance for mermaid multiplier

    public GameLogic(GameState gameState, SlotMachine slotMachine) {
        this.random = new Random();
        this.gameState = gameState;
        this.slotMachine = slotMachine;
    }

    public void spin() {
        if (gameState.isSpinning() || gameState.getBalance() < gameState.getTotalBet()) {
            return;
        }

        gameState.setSpinning(true);
        gameState.updateBalance(-gameState.getTotalBet());
        slotMachine.updateBalanceDisplay();

        CompletableFuture.runAsync(() -> {
            try {
                // Animate spinning
                for (int i = 0; i < 20; i++) {
                    final int iteration = i;
                    SwingUtilities.invokeLater(() -> {
                        for (int row = 0; row < ROWS; row++) {
                            for (int col = 0; col < COLS; col++) {
                                slotMachine.updateSymbol(row, col, getRandomSymbol().getEmoji());
                            }
                        }
                    });
                    Thread.sleep(50 + (iteration * 10));
                }

                // Final symbols
                String[][] finalGrid = new String[ROWS][COLS];
                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        Symbol symbol = getRandomSymbol();
                        finalGrid[row][col] = symbol.getEmoji();
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    for (int row = 0; row < ROWS; row++) {
                        for (int col = 0; col < COLS; col++) {
                            slotMachine.updateSymbol(row, col, finalGrid[row][col]);
                        }
                    }
                });

                // Calculate wins with cascading
                checkConsecutiveWins(finalGrid, gameState.getTotalBet());

                gameState.setSpinning(false);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private Symbol getRandomSymbol() {
        double value = random.nextDouble();
        double cumulative = 0.0;
        
        for (Symbol symbol : SYMBOLS) {
            cumulative += symbol.getProbability();
            if (value <= cumulative) {
                return symbol;
            }
        }
        
        return SYMBOLS[SYMBOLS.length - 1];
    }

    private void checkConsecutiveWins(String[][] grid, double bet) {
        StringBuilder totalWinMessage = new StringBuilder();
        calculateWins(grid, bet, totalWinMessage, 0.0, 1);
    }

    private void calculateWins(String[][] grid, double bet, StringBuilder totalWinMessage, 
                             double totalWinsSoFar, int consecutiveWinCount) {
        // Count occurrences of each symbol
        int[] symbolCounts = new int[SYMBOLS.length];
        boolean[][] matchedPositions = new boolean[ROWS][COLS];
        boolean hasWin = false;

        // Count symbols
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String symbol = grid[row][col];
                for (int k = 0; k < SYMBOLS.length; k++) {
                    if (symbol.equals(SYMBOLS[k].getEmoji())) {
                        symbolCounts[k]++;
                        break;
                    }
                }
            }
        }

        // Check for wins (8 or more of any symbol)
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (symbolCounts[i] >= 8) {
                hasWin = true;
                // Mark all matching symbols
                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        if (grid[row][col].equals(SYMBOLS[i].getEmoji())) {
                            matchedPositions[row][col] = true;
                        }
                    }
                }
            }
        }

        if (!hasWin) {
            if (consecutiveWinCount > 1) {
                slotMachine.showWinMessage(String.format("%s\nTotal Consecutive Wins: %d\nTotal Win: $%.2f!", 
                    totalWinMessage.toString(), consecutiveWinCount - 1, totalWinsSoFar));
            }
            mermaidChance += 0.05; // Increase chance by 5% on loss
            slotMachine.updateMermaidChance(mermaidChance);
            return;
        }

        double currentRoundWin = 0.0;
        StringBuilder winMessage = new StringBuilder();
        if (consecutiveWinCount > 1) {
            winMessage.append(String.format("Consecutive Win #%d!\n\n", consecutiveWinCount));
        }
        winMessage.append("Wins:\n");

        // Calculate wins for each symbol
        for (Symbol symbol : SYMBOLS) {
            int count = 0;
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (grid[row][col].equals(symbol.getEmoji()) && matchedPositions[row][col]) {
                        count++;
                    }
                }
            }
            if (count >= 8) {
                double multiplier;
                if (count <= 9) multiplier = symbol.getPayoutMultiplier(3); // Use first multiplier
                else if (count <= 11) multiplier = symbol.getPayoutMultiplier(4); // Use second multiplier
                else multiplier = symbol.getPayoutMultiplier(5); // Use third multiplier

                double win = bet * multiplier;
                currentRoundWin += win;
                winMessage.append(String.format("%s x%d: $%.2f\n", symbol.getEmoji(), count, win));

                // Play jackpot sound for treasure wins, small win sound for others
                if (symbol.getEmoji().equals("💎")) {
                    slotMachine.playSound("jackpot");
                } else if (!symbol.getEmoji().equals("💎") && !winMessage.toString().contains("jackpot")) {
                    slotMachine.playSound("smallwin");
                }
            }
        }

        // Check for mermaid multiplier
        if (random.nextDouble() < mermaidChance) {
            double mermaidMultiplier = (random.nextDouble() * 5) + 2;
            currentRoundWin *= mermaidMultiplier;
            winMessage.append(String.format("\nMermaid Multiplier: x%.2f!", mermaidMultiplier));
            mermaidChance = 0.1; // Reset chance after getting mermaid multiplier
            slotMachine.updateMermaidChance(mermaidChance);
        } else if (consecutiveWinCount > 1) {
            mermaidChance += 0.1; // Increase chance by 10% on consecutive win without mermaid
            slotMachine.updateMermaidChance(mermaidChance);
        }

        double newTotalWins = totalWinsSoFar + currentRoundWin;

        if (totalWinMessage.length() > 0) {
            totalWinMessage.append("\n\n");
        }
        totalWinMessage.append(winMessage);

        // Update balance and show win
        gameState.updateBalance(currentRoundWin);
        slotMachine.updateBalanceDisplay();
        slotMachine.showWinMessage(winMessage.toString());

        // Animate and cascade
        animateMatchesAndCascade(matchedPositions, totalWinMessage.toString(), 
            newTotalWins, grid, bet, totalWinMessage, newTotalWins, consecutiveWinCount + 1);
    }

    private void animateMatchesAndCascade(boolean[][] matchedPositions, String winMessage, 
            double totalWin, String[][] originalGrid, double bet,
            StringBuilder totalWinMessage, double totalWins, int nextConsecutiveWinCount) {
        CompletableFuture.runAsync(() -> {
            try {
                // Scale animation for matched symbols
                for (int scale = 130; scale >= 70; scale -= 10) {
                    final int finalScale = scale;
                    SwingUtilities.invokeLater(() -> {
                        for (int row = 0; row < ROWS; row++) {
                            for (int col = 0; col < COLS; col++) {
                                if (matchedPositions[row][col]) {
                                    slotMachine.scaleSymbol(row, col, finalScale);
                                }
                            }
                        }
                    });
                    Thread.sleep(50);
                }

                // Create new grid and remove matched symbols
                String[][] newGrid = new String[ROWS][COLS];
                for (int row = 0; row < ROWS; row++) {
                    System.arraycopy(originalGrid[row], 0, newGrid[row], 0, COLS);
                }

                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        if (matchedPositions[row][col]) {
                            newGrid[row][col] = "";
                            final int r = row;
                            final int c = col;
                            SwingUtilities.invokeLater(() -> {
                                slotMachine.updateSymbol(r, c, "");
                                slotMachine.resetSymbolScale(r, c);
                            });
                        }
                    }
                }
                Thread.sleep(300);

                // Cascade symbols down
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
                    // Fill empty spaces with new symbols
                    for (int row = emptyRow; row >= 0; row--) {
                        newGrid[row][col] = getRandomSymbol().getEmoji();
                    }
                }

                // Update display with new grid
                SwingUtilities.invokeLater(() -> {
                    for (int row = 0; row < ROWS; row++) {
                        for (int col = 0; col < COLS; col++) {
                            slotMachine.updateSymbol(row, col, newGrid[row][col]);
                        }
                    }
                });
                Thread.sleep(500);

                // Check for new wins
                calculateWins(newGrid, bet, totalWinMessage, totalWins, nextConsecutiveWinCount);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
}
