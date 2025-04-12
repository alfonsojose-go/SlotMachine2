import java.awt.*;
import javax.swing.*;
import java.text.NumberFormat;

public class SlotMachine extends JFrame {
    // UI Components
    public JLabel[][] reels = new JLabel[6][5];
    public JButton spinButton = new JButton("SPIN");
    public JButton increaseVol = new JButton("+");
    public JButton decreaseVol = new JButton("-");
    public JLabel accountLabel = new JLabel("Player:");
    public JLabel accountBalance = new JLabel("$00.00");
    public JLabel lblInsertCoin = new JLabel("Insert Coin:");
    public JLabel lblbet = new JLabel("Bet Amount:");
    public JLabel lblVolume = new JLabel("Vol:");
    public JTextField insertCoin = new JTextField();
    public JTextField betField = new JTextField();
    public JTextField volumeVal = new JTextField();

    // Image paths
    private final String BG_PATH = "Assets/scales.jpg";
    private final String LEFT_DECOR = "Assets/leftDecor.jpg";
    private final String RIGHT_DECOR = "Assets/rightDecor.jpg";
    private final String TOP_DECOR = "Assets/topDecor.jpg";

    // Game components
    private GameState gameState;
    private GameLogic gameLogic;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private static final Font SYMBOL_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 40);

    // Sound Manager
    private final SlotSoundManager soundManager = new SlotSoundManager();
    private final String BACKGROUND_MUSIC = "bg_music"; // Identifier for background music

    // Constructor for the SlotMachine class
    public SlotMachine() {
        // Frame setup
        setTitle("Secrets of the Mermaid");
        setSize(1000, 600); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null); // Use absolute positioning

        // Load and loop background music
        soundManager.load(BACKGROUND_MUSIC, "Audio/backgroundmusic.wav");
        soundManager.setGlobalVolume(0.5f); // Default to 50%
        soundManager.loop(BACKGROUND_MUSIC);

        soundManager.load("spin", "Audio/buttonClick.wav");

        // Initialize game components
        gameState = new GameState();
        gameLogic = new GameLogic(gameState, this);

        // Main panel with background
        JPanel mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image bg = new ImageIcon(BG_PATH).getImage();
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setBounds(0, 0, 1000, 600);
        add(mainPanel);

        // Top decoration
        JLabel topPanel = new JLabel(new ImageIcon(TOP_DECOR));
        topPanel.setBounds(0, 0, 1000, 100);
        mainPanel.add(topPanel);

        // Side decorations (15% width)
        int sidePanelWidth = (int)(getWidth() * 0.15);

        // Left decoration
        JLabel leftPanel = new JLabel(new ImageIcon(LEFT_DECOR));
        leftPanel.setBounds(0, 100, sidePanelWidth, 500);
        mainPanel.add(leftPanel);

        // Right decoration
        JLabel rightPanel = new JLabel(new ImageIcon(RIGHT_DECOR));
        rightPanel.setBounds(850, 100, sidePanelWidth, 500);
        mainPanel.add(rightPanel);

        // Create 5x6 grid of reels
        JPanel reelsPanel = new JPanel(new GridLayout(ROWS, COLS, 10, 10)); // 5 rows, 6 columns
        reelsPanel.setBounds(150 + 15, 120, 700 - 30, 300);
        reelsPanel.setBackground(Color.WHITE);
        reelsPanel.setOpaque(true);
        reelsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Initialize reels
        for (int row = 0; row < ROWS; row++) { // 5 rows
            for (int col = 0; col < COLS; col++) { // 6 columns
                reels[row][col] = new JLabel("🌊", JLabel.CENTER);
                reels[row][col].setFont(SYMBOL_FONT);
                reels[row][col].setOpaque(true);
                reels[row][col].setBackground(Color.WHITE);
                reels[row][col].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                reelsPanel.add(reels[row][col]);
            }
        }
        mainPanel.add(reelsPanel);

        // Add mermaid chance label
        mermaidChanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mermaidChanceLabel.setForeground(Color.BLUE);
        mermaidChanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel mermaidPanel = new JPanel();
        mermaidPanel.add(mermaidChanceLabel);
        mermaidPanel.setBounds(150, 80, 700, 30);
        mainPanel.add(mermaidPanel);

        // Control panel area
        int controlY = 440;
        
        // Balance info
        accountLabel.setForeground(Color.WHITE);
        accountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        accountLabel.setBounds(170, controlY, 60, 25);
        mainPanel.add(accountLabel);
        
        accountBalance.setForeground(Color.YELLOW);
        accountBalance.setFont(new Font("Arial", Font.BOLD, 14));
        accountBalance.setBounds(240, controlY, 120, 25);
        mainPanel.add(accountBalance);
        
        // Coin value controls
        lblInsertCoin.setForeground(Color.WHITE);
        lblInsertCoin.setFont(new Font("Arial", Font.BOLD, 14));
        lblInsertCoin.setBounds(170, controlY + 40, 90, 25);
        mainPanel.add(lblInsertCoin);
        
        insertCoin.setFont(new Font("Arial", Font.PLAIN, 14));
        insertCoin.setBounds(270, controlY + 40, 150, 25);
        mainPanel.add(insertCoin);
        
        // Bet multiplier controls
        lblbet.setForeground(Color.WHITE);
        lblbet.setFont(new Font("Arial", Font.BOLD, 14));
        lblbet.setBounds(440, controlY + 40, 90, 25);
        mainPanel.add(lblbet);
        
        betField.setFont(new Font("Arial", Font.PLAIN, 14));
        betField.setBounds(530, controlY + 40, 150, 25);
        mainPanel.add(betField);

        // Spin button
        spinButton.setFont(new Font("Arial", Font.BOLD, 16));
        spinButton.setBackground(new Color(255, 215, 0));
        spinButton.setForeground(Color.BLACK);
        spinButton.setBounds(700, controlY + 40, 120, 35);
        mainPanel.add(spinButton);
        spinButton.addActionListener(e -> {
            soundManager.play("spin");
        });
        
        
        // Volume controls
        lblVolume.setForeground(Color.WHITE);
        lblVolume.setFont(new Font("Arial", Font.BOLD, 14));
        lblVolume.setBounds(170, controlY + 80, 40, 25);
        mainPanel.add(lblVolume);
        
        volumeVal.setFont(new Font("Arial", Font.PLAIN, 14));
        volumeVal.setText("50");
        volumeVal.setBounds(220, controlY + 80, 50, 25);
        mainPanel.add(volumeVal);
        
        // Volume buttons
        decreaseVol.setFont(new Font("Arial", Font.BOLD, 14));
        decreaseVol.setBounds(280, controlY + 80, 60, 25);
        mainPanel.add(decreaseVol);
        
        increaseVol.setFont(new Font("Arial", Font.BOLD, 14));
        increaseVol.setBounds(330, controlY + 80, 60, 25);
        mainPanel.add(increaseVol);
    

        // Volume button actions
        increaseVol.addActionListener(e -> {
            try {
                int current = Integer.parseInt(volumeVal.getText());
                if (current < 100) {
                    current += 5;
                    volumeVal.setText(String.valueOf(current));
                    soundManager.setGlobalVolume(current / 100f);
                }
            } catch (NumberFormatException ignored) {}
        });

        decreaseVol.addActionListener(e -> {
            try {
                int current = Integer.parseInt(volumeVal.getText());
                if (current > 0) {
                    current -= 5;
                    volumeVal.setText(String.valueOf(current));
                    soundManager.setGlobalVolume(current / 100f);
                }
            } catch (NumberFormatException ignored) {}
        });
    }

    // Method to set the player's name
    public void setPlayerName(String username) {
        accountLabel.setText(username);  // Replace "Player" with the username
        // Setup button actions
        setupButtonActions();
        
        // Initial display updates
        updateDisplays();
    }

    private void setupButtonActions() {
        spinButton.addActionListener(_ -> gameLogic.spin());
        
        increaseCoin.addActionListener(_ -> {
            if (!gameState.isSpinning()) {
                gameState.setCoinValue(gameState.getCoinValue() + 0.05);
                updateDisplays();
            }
        });
        
        decreaseCoin.addActionListener(_ -> {
            if (!gameState.isSpinning()) {
                gameState.setCoinValue(gameState.getCoinValue() - 0.05);
                updateDisplays();
            }
        });
        
        increaseBet.addActionListener(_ -> {
            if (!gameState.isSpinning()) {
                gameState.setBetMultiplier(gameState.getBetMultiplier() + 1);
                updateDisplays();
            }
        });
        
        decreaseBet.addActionListener(_ -> {
            if (!gameState.isSpinning()) {
                gameState.setBetMultiplier(gameState.getBetMultiplier() - 1);
                updateDisplays();
            }
        });
    }

    private void updateDisplays() {
        accountBalance.setText(currencyFormat.format(gameState.getBalance()));
        coinValueDisplay.setText(currencyFormat.format(gameState.getCoinValue()));
        betMultiplierDisplay.setText(gameState.getBetMultiplier() + "x");
        totalBetDisplay.setText("Total Bet: " + currencyFormat.format(gameState.getTotalBet()));
    }

    public void updateBalanceDisplay() {
        SwingUtilities.invokeLater(() -> {
            accountBalance.setText(currencyFormat.format(gameState.getBalance()));
        });
    }

    public void updateSymbol(int row, int col, String symbol) {
        reels[row][col].setText(symbol);
    }

    public void scaleSymbol(int row, int col, int scale) {
        Font currentFont = reels[row][col].getFont();
        reels[row][col].setFont(currentFont.deriveFont((float)(SYMBOL_FONT.getSize() * scale / 100)));
    }

    public void resetSymbolScale(int row, int col) {
        reels[row][col].setFont(SYMBOL_FONT);
    }

    public void highlightSymbol(int row, int col, boolean highlight) {
        reels[row][col].setForeground(highlight ? Color.RED : Color.BLACK);
    }

    public void showWinMessage(String message) {
        messageLabel.setText(message);
        Timer timer = new Timer(3000, _ -> messageLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }

    public void updateMermaidChance(double chance) {
        SwingUtilities.invokeLater(() -> 
            mermaidChanceLabel.setText(String.format("Mermaid Chance: %.0f%%", chance * 100))
        );
    }

    public static void main(String[] args) {
        // Set system properties for better emoji rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> new SlotMachine().setVisible(true));
    }
    
}
