import java.awt.*;
import javax.swing.*;
import java.text.NumberFormat;

public class SlotMachine extends JFrame {
    // UI Components
    private static final int ROWS = 5;
    private static final int COLS = 6;
    private JLabel[][] reels = new JLabel[ROWS][COLS]; // 5 rows x 6 columns
    private JButton spinButton = new JButton("SPIN");
    private JLabel accountLabel = new JLabel("Balance:");
    private JLabel accountBalance = new JLabel("$1000.00");
    private JLabel lblInsertCoin = new JLabel("Coin Value:");
    private JLabel lblbet = new JLabel("Bet Amount:");
    private JButton increaseCoin = new JButton("+");
    private JButton decreaseCoin = new JButton("-");
    private JButton increaseBet = new JButton("+");
    private JButton decreaseBet = new JButton("-");
    private JLabel coinValueDisplay = new JLabel("$0.05");
    private JLabel betMultiplierDisplay = new JLabel("1x");
    private JLabel totalBetDisplay = new JLabel("Total Bet: $0.05");
    private JLabel messageLabel = new JLabel("");
    private JLabel mermaidChanceLabel = new JLabel("Mermaid Chance: 10%");
    
    // Volume controls
    private JLabel lblVolume = new JLabel("Vol:");
    private JLabel volumeVal = new JLabel();
    private JButton decreaseVol = new JButton("-");
    private JButton increaseVol = new JButton("+");
    
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

    public SlotMachine() {
        // Frame setup
        setTitle("Secrets of the Mermaid");
        setSize(1000, 600); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null); // Use absolute positioning

        // Load and loop background music
        soundManager.load(BACKGROUND_MUSIC, "Audio/backgroundmusic.wav");
        soundManager.load("spin", "Audio/buttonClick.wav");
        soundManager.load("jackpot", "Audio/jackpot.wav");
        soundManager.load("smallwin", "Audio/smallwin.wav");
        
        soundManager.setGlobalVolume(0.5f); // Default to 50%
        soundManager.loop(BACKGROUND_MUSIC);

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

        // Total bet display
        totalBetDisplay.setForeground(Color.WHITE);
        totalBetDisplay.setFont(new Font("Arial", Font.BOLD, 14));
        totalBetDisplay.setBounds(370, controlY, 150, 25);
        mainPanel.add(totalBetDisplay);

        // Mermaid chance label
        mermaidChanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mermaidChanceLabel.setForeground(new Color(0, 191, 255));
        mermaidChanceLabel.setBounds(530, controlY, 180, 25);
        mainPanel.add(mermaidChanceLabel);

        // Message label
        messageLabel.setForeground(Color.YELLOW);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBounds(300, controlY + 80, 600, 25);
        mainPanel.add(messageLabel);

        // Coin value controls
        lblInsertCoin.setForeground(Color.WHITE);
        lblInsertCoin.setFont(new Font("Arial", Font.BOLD, 14));
        lblInsertCoin.setBounds(170, controlY + 40, 90, 25);
        mainPanel.add(lblInsertCoin);
        
        coinValueDisplay.setForeground(Color.WHITE);
        coinValueDisplay.setFont(new Font("Arial", Font.BOLD, 14));
        coinValueDisplay.setBounds(270, controlY + 40, 60, 25);
        mainPanel.add(coinValueDisplay);
        
        decreaseCoin.setFont(new Font("Arial", Font.BOLD, 14));
        decreaseCoin.setBounds(340, controlY + 40, 45, 25);
        mainPanel.add(decreaseCoin);
        
        increaseCoin.setFont(new Font("Arial", Font.BOLD, 14));
        increaseCoin.setBounds(385, controlY + 40, 45, 25);
        mainPanel.add(increaseCoin);
        
        // Bet multiplier controls
        lblbet.setForeground(Color.WHITE);
        lblbet.setFont(new Font("Arial", Font.BOLD, 14));
        lblbet.setBounds(440, controlY + 40, 90, 25);
        mainPanel.add(lblbet);
        
        betMultiplierDisplay.setForeground(Color.WHITE);
        betMultiplierDisplay.setFont(new Font("Arial", Font.BOLD, 14));
        betMultiplierDisplay.setBounds(540, controlY + 40, 45, 25);
        mainPanel.add(betMultiplierDisplay);
        
        decreaseBet.setFont(new Font("Arial", Font.BOLD, 14));
        decreaseBet.setBounds(595, controlY + 40, 45, 25);
        mainPanel.add(decreaseBet);
        
        increaseBet.setFont(new Font("Arial", Font.BOLD, 14));
        increaseBet.setBounds(640, controlY + 40, 45, 25);
        mainPanel.add(increaseBet);

        // Spin button
        spinButton.setFont(new Font("Arial", Font.BOLD, 16));
        spinButton.setBackground(new Color(255, 215, 0));
        spinButton.setForeground(Color.BLACK);
        spinButton.setBounds(700, controlY + 40, 120, 35);
        mainPanel.add(spinButton);
        spinButton.addActionListener(e -> {
            soundManager.play("spin");
        });

        // Volume controls - positioned on left side
        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        volumePanel.setOpaque(false); // Make panel transparent
        volumePanel.setBounds(170, controlY + 80, 170, 25);
        mainPanel.add(volumePanel);

        lblVolume.setForeground(Color.WHITE);
        lblVolume.setFont(new Font("Arial", Font.BOLD, 14));
        volumePanel.add(lblVolume);
        
        volumeVal.setForeground(Color.WHITE);
        volumeVal.setFont(new Font("Arial", Font.BOLD, 14));
        volumeVal.setText("50");
        volumeVal.setPreferredSize(new Dimension(30, 25));
        volumeVal.setHorizontalAlignment(JTextField.CENTER); // Center the text
        volumePanel.add(volumeVal);
        
        decreaseVol.setFont(new Font("Arial", Font.BOLD, 14));
        decreaseVol.setPreferredSize(new Dimension(45, 25));
        volumePanel.add(decreaseVol);
        
        increaseVol.setFont(new Font("Arial", Font.BOLD, 14));
        increaseVol.setPreferredSize(new Dimension(45, 25));
        volumePanel.add(increaseVol);

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

        // Setup button actions
        setupButtonActions();
        
        // Initial display updates
        updateDisplays();
    }

    public void setPlayerName(String username) {
        accountLabel.setText(username);  // Replace "Player" with the username
    }

    private void setupButtonActions() {
        spinButton.addActionListener(e -> gameLogic.spin());
        
        increaseCoin.addActionListener(e -> {
            if (!gameState.isSpinning()) {
                gameState.setCoinValue(gameState.getCoinValue() + 1.00);
                updateDisplays();
            }
        });
        
        decreaseCoin.addActionListener(e -> {
            if (!gameState.isSpinning()) {
                gameState.setCoinValue(gameState.getCoinValue() - 1.00);
                updateDisplays();
            }
        });
        
        increaseBet.addActionListener(e -> {
            if (!gameState.isSpinning() && gameState.getBetMultiplier() < 10) {
                gameState.setBetMultiplier(gameState.getBetMultiplier() + 1);
                updateDisplays();
            }
        });
        
        decreaseBet.addActionListener(e -> {
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
        Timer timer = new Timer(3000, e -> messageLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }

    public void updateMermaidChance(double chance) {
        SwingUtilities.invokeLater(() -> 
            mermaidChanceLabel.setText(String.format("Mermaid Chance: %.0f%%", chance * 100))
        );
    }

    public void playSound(String soundName) {
        soundManager.play(soundName);
    }

    public static void main(String[] args) {
        // Set system properties for better emoji rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> AccountLogin.main(args));
    }
    
}
