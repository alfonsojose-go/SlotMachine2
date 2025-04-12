import java.awt.*;
import javax.swing.*;

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
    private static final String[] SYMBOLS = {
        "🐟", "🦀", "🐬", "🐚", "⚓", "🔱", "💎", "🚢", "💰"
    };

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
        JPanel reelsPanel = new JPanel(new GridLayout(5, 6, 10, 10));
        reelsPanel.setBounds(150 + 15, 120, 700 - 30, 300);
        reelsPanel.setBackground(Color.WHITE);
        reelsPanel.setOpaque(true);
        reelsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Initialize reels with random symbols
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                reels[col][row] = new JLabel(SYMBOLS[(int)(Math.random() * SYMBOLS.length)], JLabel.CENTER);
                reels[col][row].setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                reels[col][row].setOpaque(true);
                reels[col][row].setBackground(Color.WHITE);
                reels[col][row].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                reelsPanel.add(reels[col][row]);
            }
        }
        mainPanel.add(reelsPanel);

        // Control panel area
        int controlY = 440;
        
        // Player info
        accountLabel.setForeground(Color.WHITE);
        accountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        accountLabel.setBounds(170, controlY, 60, 25);
        mainPanel.add(accountLabel);
        
        accountBalance.setForeground(Color.YELLOW);
        accountBalance.setFont(new Font("Arial", Font.BOLD, 14));
        accountBalance.setBounds(240, controlY, 80, 25);
        mainPanel.add(accountBalance);
        
        // Insert coin section
        lblInsertCoin.setForeground(Color.WHITE);
        lblInsertCoin.setFont(new Font("Arial", Font.BOLD, 14));
        lblInsertCoin.setBounds(170, controlY + 40, 90, 25);
        mainPanel.add(lblInsertCoin);
        
        insertCoin.setFont(new Font("Arial", Font.PLAIN, 14));
        insertCoin.setBounds(270, controlY + 40, 150, 25);
        mainPanel.add(insertCoin);
        
        // Bet amount section
        lblbet.setForeground(Color.WHITE);
        lblbet.setFont(new Font("Arial", Font.BOLD, 14));
        lblbet.setBounds(430, controlY + 40, 90, 25);
        mainPanel.add(lblbet);
        
        betField.setFont(new Font("Arial", Font.PLAIN, 14));
        betField.setBounds(530, controlY + 40, 150, 25);
        mainPanel.add(betField);

        // Spin button
        spinButton.setFont(new Font("Arial", Font.BOLD, 16));
        spinButton.setBackground(new Color(255, 215, 0));
        spinButton.setForeground(Color.BLACK);
        spinButton.setBounds(700, controlY + 40, 120, 35);// Spin button
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
    }
}
