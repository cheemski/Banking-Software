package Code;

public class Savings extends Account {
    private double interestRate;

    public Savings() {
        setAccountType(TYPE_SAVINGS);
    }

    public Savings(int userId, String accountNumber, double balance, double interestRate, String status) {
        super(userId, accountNumber, TYPE_SAVINGS, balance, status);
        this.interestRate = interestRate;
    }

    @Override
    public double getInterestRate() {
        return interestRate;
    }

    @Override
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
}
