package Code;

public class CurrentAccount extends Account {
    private double overdraftLimit;
    private double overdraftFee;

    public CurrentAccount() {
        setAccountType(TYPE_CURRENT);
    }

    public CurrentAccount(User user, String accountNumber, double balance, double overdraftLimit, double overdraftFee, String status) {
        super(user, accountNumber, TYPE_CURRENT, balance, status);
        this.overdraftLimit = overdraftLimit;
        this.overdraftFee = overdraftFee;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftFee() {
        return overdraftFee;
    }

    public void setOverdraftFee(double overdraftFee) {
        this.overdraftFee = overdraftFee;
    }

    public double getAvailableBalance() {
        return getBalance() + overdraftLimit;
    }
}
