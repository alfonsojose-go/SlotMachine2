public class GameState {
    private double balance;
    private double coinValue;
    private int betMultiplier;
    private double totalBet;
    private boolean isSpinning;
    private double mermaidChance;

    public GameState() {
        this.balance = 1000.0;
        this.coinValue = 0.05;
        this.betMultiplier = 1;
        this.totalBet = coinValue * betMultiplier;
        this.isSpinning = false;
        this.mermaidChance = 0.1;
    }

    public double getBalance() {
        return balance;
    }

    public void updateBalance(double amount) {
        this.balance += amount;
    }

    public double getCoinValue() {
        return coinValue;
    }

    public void setCoinValue(double value) {
        this.coinValue = Math.max(0.05, Math.min(1.00, value));
        updateTotalBet();
    }

    public int getBetMultiplier() {
        return betMultiplier;
    }

    public void setBetMultiplier(int multiplier) {
        this.betMultiplier = Math.max(1, Math.min(10, multiplier));
        updateTotalBet();
    }

    public double getTotalBet() {
        return totalBet;
    }

    private void updateTotalBet() {
        this.totalBet = coinValue * betMultiplier;
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public void setSpinning(boolean spinning) {
        this.isSpinning = spinning;
    }

    public double getMermaidChance() {
        return mermaidChance;
    }

    public void updateMermaidChance(double change) {
        this.mermaidChance = Math.max(0.1, Math.min(0.5, mermaidChance + change));
    }
}
