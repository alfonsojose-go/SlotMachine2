public class Symbol {
    private String emoji;
    private String name;
    private double probability;
    private double[] payoutMultipliers;

    public Symbol(String emoji, String name, double probability, double[] payoutMultipliers) {
        this.emoji = emoji;
        this.name = name;
        this.probability = probability;
        this.payoutMultipliers = payoutMultipliers;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getName() {
        return name;
    }

    public double getProbability() {
        return probability;
    }

    public double getPayoutMultiplier(int matches) {
        if (matches < 3 || matches > 5) {
            return 0.0;
        }
        return payoutMultipliers[matches - 3];
    }
}
