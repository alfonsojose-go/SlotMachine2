import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;

public class GameLogic {
    // Grid dimensions
    private static final int ROWS = 5;
    private static final int COLS = 6;

    // Animation constants
    private static final int SPIN_FRAMES = 20;
    private static final int SPIN_DELAY_BASE = 50;
    private static final int SCALE_MAX = 130;
    private static final int SCALE_MIN = 70;
    private static final int SCALE_STEP = 10;
    private static final int CASCADE_DELAY = 300;
    private static final int UPDATE_DELAY = 500;

    // Game constants
    private static final int MIN_MATCHES = 8;
    private static final double MIN_MERMAID_CHANCE = 0.1;
    private static final double MAX_MERMAID_CHANCE = 1.0;
    private static final double MERMAID_MULTIPLIER_MIN = 2.0;
    private static final double MERMAID_MULTIPLIER_RANGE = 5.0;

    // Symbol definitions with their probabilities and payouts
    private static final Symbol[] SYMBOLS = {
        new Symbol("🐟", "Fish", 0.30, new double[]{0.25, 0.75, 2.00}),
        new Symbol("🦀", "Crab", 0.30, new double[]{0.40, 0.90, 4.00}),
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
    private double mermaidChance = MIN_MERMAID_CHANCE;

    public GameLogic(GameState gameState, SlotMachine slotMachine) {
        this.random = new Random();
        this.gameState = gameState;
        this.slotMachine = slotMachine;
    }

    /**
     * Initiates a spin if the player has sufficient balance and is not already spinning.
     * Animates the spin and calculates any wins.
     */
    public void spin() {
        if (gameState.isSpinning() || gameState.getBalance() < gameState.getTotalBet()) {
            return;
        }

        gameState.setSpinning(true);
        gameState.updateBalance(-gameState.getTotalBet());
        slotMachine.updateBalanceDisplay();

        CompletableFuture.runAsync(() -> {
            try {
                animateSpinning();
                String[][] finalGrid = generateFinalGrid();
                updateGridDisplay(finalGrid);
                checkConsecutiveWins(finalGrid, gameState.getTotalBet());
                gameState.setSpinning(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Animates the spinning reels.
     */
    private void animateSpinning() throws InterruptedException {
        for (int i = 0; i < SPIN_FRAMES; i++) {
            final int iteration = i;
            SwingUtilities.invokeLater(() -> {
                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        slotMachine.updateSymbol(row, col, getRandomSymbol().getEmoji());
                    }
                }
            });
            Thread.sleep(SPIN_DELAY_BASE + (iteration * 10));
        }
    }

    /**
     * Generates the final grid of symbols after a spin.
     */
    private String[][] generateFinalGrid() {
        String[][] grid = new String[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = getRandomSymbol().getEmoji();
            }
        }
        return grid;
    }

    /**
     * Updates the display with the given grid of symbols.
     */
    private void updateGridDisplay(String[][] grid) {
        SwingUtilities.invokeLater(() -> {
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    slotMachine.updateSymbol(row, col, grid[row][col]);
                }
            }
        });
    }

    /**
     * Returns a random symbol based on their probabilities.
     */
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

    /**
     * Calculates wins for the current grid and handles cascading.
     */
    private void calculateWins(String[][] grid, double bet, StringBuilder totalWinMessage, 
                             double totalWinsSoFar, int consecutiveWinCount) {
        int[] symbolCounts = countSymbols(grid);
        boolean[][] matchedPositions = new boolean[ROWS][COLS];
        boolean hasWin = markMatchingPositions(grid, symbolCounts, matchedPositions);

        if (!hasWin) {
            if (consecutiveWinCount > 1) {
                slotMachine.showWinMessage(String.format("Total Amount Won: $%.2f", totalWinsSoFar), 8000);
            }
            updateMermaidChance(0.05); // Increase chance by 5% on loss
            return;
        }

        double currentRoundWin = calculateWinAmount(grid, symbolCounts, matchedPositions, bet);
        StringBuilder winMessage = buildWinMessage(grid, symbolCounts, matchedPositions, bet);
        playWinSounds(grid, symbolCounts, matchedPositions);

        // Apply mermaid multiplier if triggered
        if (random.nextDouble() < mermaidChance) {
            double mermaidMultiplier = MERMAID_MULTIPLIER_MIN + (random.nextDouble() * MERMAID_MULTIPLIER_RANGE);
            currentRoundWin *= mermaidMultiplier;
            updateMermaidChance(-0.9); // Reset chance after getting mermaid multiplier
            slotMachine.showWinMessage(String.format("Mermaid Multiplier (%.1fx): You Won $%.2f!", mermaidMultiplier, currentRoundWin), 5000);
        } else {
            slotMachine.showWinMessage(String.format("You Won $%.2f!", currentRoundWin));
            if (consecutiveWinCount > 1) {
                updateMermaidChance(0.1); // Increase chance by 10% on consecutive win without mermaid
            }
        }

        double newTotalWins = totalWinsSoFar + currentRoundWin;
        appendWinMessage(totalWinMessage, winMessage);
        updateGameState(currentRoundWin);
        animateMatchesAndCascade(matchedPositions, totalWinMessage.toString(), newTotalWins, grid, bet, 
                               totalWinMessage, newTotalWins, consecutiveWinCount + 1);
    }

    /**
     * Counts occurrences of each symbol in the grid.
     */
    private int[] countSymbols(String[][] grid) {
        int[] counts = new int[SYMBOLS.length];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String symbol = grid[row][col];
                for (int k = 0; k < SYMBOLS.length; k++) {
                    if (symbol.equals(SYMBOLS[k].getEmoji())) {
                        counts[k]++;
                        break;
                    }
                }
            }
        }
        return counts;
    }

    /**
     * Marks positions of matching symbols and returns whether there was a win.
     */
    private boolean markMatchingPositions(String[][] grid, int[] symbolCounts, boolean[][] matchedPositions) {
        boolean hasWin = false;
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (symbolCounts[i] >= MIN_MATCHES) {
                hasWin = true;
                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        if (grid[row][col].equals(SYMBOLS[i].getEmoji())) {
                            matchedPositions[row][col] = true;
                        }
                    }
                }
            }
        }
        return hasWin;
    }

    /**
     * Calculates the total win amount for matched symbols.
     */
    private double calculateWinAmount(String[][] grid, int[] symbolCounts, boolean[][] matchedPositions, double bet) {
        double totalWin = 0.0;
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (symbolCounts[i] >= MIN_MATCHES) {
                double multiplier;
                if (symbolCounts[i] <= 9) multiplier = SYMBOLS[i].getPayoutMultiplier(3);
                else if (symbolCounts[i] <= 11) multiplier = SYMBOLS[i].getPayoutMultiplier(4);
                else multiplier = SYMBOLS[i].getPayoutMultiplier(5);
                totalWin += bet * multiplier;
            }
        }
        return totalWin;
    }

    /**
     * Builds the win message showing individual symbol wins.
     */
    private StringBuilder buildWinMessage(String[][] grid, int[] symbolCounts, boolean[][] matchedPositions, double bet) {
        StringBuilder message = new StringBuilder();
        message.append("Wins:\n");
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (symbolCounts[i] >= MIN_MATCHES) {
                double multiplier;
                if (symbolCounts[i] <= 9) multiplier = SYMBOLS[i].getPayoutMultiplier(3);
                else if (symbolCounts[i] <= 11) multiplier = SYMBOLS[i].getPayoutMultiplier(4);
                else multiplier = SYMBOLS[i].getPayoutMultiplier(5);
                message.append(String.format("%s x%d: $%.2f\n", SYMBOLS[i].getEmoji(), symbolCounts[i], bet * multiplier));
            }
        }
        return message;
    }

    /**
     * Plays appropriate win sounds based on the symbols matched.
     */
    private void playWinSounds(String[][] grid, int[] symbolCounts, boolean[][] matchedPositions) {
        boolean hasJackpot = false;
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (symbolCounts[i] >= MIN_MATCHES && SYMBOLS[i].getEmoji().equals("💎")) {
                hasJackpot = true;
                slotMachine.playSound("jackpot");
                break;
            }
        }
        if (!hasJackpot) {
            slotMachine.playSound("smallwin");
        }
    }

    private void appendWinMessage(StringBuilder totalWinMessage, StringBuilder winMessage) {
        if (totalWinMessage.length() > 0) {
            totalWinMessage.append("\n\n");
        }
        totalWinMessage.append(winMessage);
    }

    private void updateGameState(double winAmount) {
        gameState.updateBalance(winAmount);
        slotMachine.updateBalanceDisplay();
    }

    /**
     * Animates matched symbols and handles cascading of new symbols.
     */
    private void animateMatchesAndCascade(boolean[][] matchedPositions, String winMessage, 
            double totalWin, String[][] originalGrid, double bet,
            StringBuilder totalWinMessage, double totalWins, int nextConsecutiveWinCount) {
        CompletableFuture.runAsync(() -> {
            try {
                animateMatchedSymbols(matchedPositions);
                String[][] newGrid = removeMatchedSymbols(matchedPositions, originalGrid);
                cascadeSymbols(newGrid);
                updateGridDisplay(newGrid);
                Thread.sleep(UPDATE_DELAY);
                calculateWins(newGrid, bet, totalWinMessage, totalWins, nextConsecutiveWinCount);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Animates the scaling of matched symbols.
     */
    private void animateMatchedSymbols(boolean[][] matchedPositions) throws InterruptedException {
        for (int scale = SCALE_MAX; scale >= SCALE_MIN; scale -= SCALE_STEP) {
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
    }

    /**
     * Removes matched symbols from the grid and returns a new grid.
     */
    private String[][] removeMatchedSymbols(boolean[][] matchedPositions, String[][] originalGrid) throws InterruptedException {
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
        Thread.sleep(CASCADE_DELAY);
        return newGrid;
    }

    /**
     * Cascades symbols down to fill empty spaces.
     */
    private void cascadeSymbols(String[][] grid) {
        for (int col = 0; col < COLS; col++) {
            int emptyRow = ROWS - 1;
            for (int row = ROWS - 1; row >= 0; row--) {
                if (!grid[row][col].isEmpty()) {
                    if (row != emptyRow) {
                        grid[emptyRow][col] = grid[row][col];
                        grid[row][col] = "";
                    }
                    emptyRow--;
                }
            }
            // Fill empty spaces with new symbols
            for (int row = emptyRow; row >= 0; row--) {
                grid[row][col] = getRandomSymbol().getEmoji();
            }
        }
    }

    private void updateMermaidChance(double change) {
        mermaidChance = Math.min(MAX_MERMAID_CHANCE, Math.max(MIN_MERMAID_CHANCE, mermaidChance + change));
        slotMachine.updateMermaidChance(mermaidChance);
    }
}
