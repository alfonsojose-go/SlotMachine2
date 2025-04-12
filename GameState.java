public class GameState {
    private double balance;
    private double coinValue;
    private int betMultiplier;
    private double totalBet;
    private boolean isSpinning;

    public GameState() {
        this.balance = 1000.0;
        this.coinValue = 1.00;
        this.betMultiplier = 1;
        this.totalBet = coinValue * betMultiplier;
        this.isSpinning = false;
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
        // Ensure coin value stays between 1.00 and 10.00
        this.coinValue = Math.max(1.00, Math.min(10.00, value));
        updateTotalBet();
    }

    public int getBetMultiplier() {
        return betMultiplier;
    }

    public void setBetMultiplier(int betMultiplier) {
        // Ensure bet multiplier stays within valid range (1-10)
        this.betMultiplier = Math.max(1, Math.min(10, betMultiplier));
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
}
