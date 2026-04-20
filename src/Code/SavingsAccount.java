package Code;

public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount() {
        setAccountType(TYPE_SAVINGS);
    }

    public SavingsAccount(User user, String accountNumber, double balance, double interestRate, String status) {
        super(user, accountNumber, TYPE_SAVINGS, balance, status);
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
