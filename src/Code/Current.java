package Code;

public class Current extends Account {
    private double overdraftLimit;
    private double overdraftFee;

    public Current() {
        setAccountType(TYPE_CURRENT);
    }

    public Current(int userId, String accountNumber, double balance, double overdraftLimit, double overdraftFee, String status) {
        super(userId, accountNumber, TYPE_CURRENT, balance, status);
        this.overdraftLimit = overdraftLimit;
        this.overdraftFee = overdraftFee;
    }

    @Override
    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    @Override
    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double getOverdraftFee() {
        return overdraftFee;
    }

    @Override
    public void setOverdraftFee(double overdraftFee) {
        this.overdraftFee = overdraftFee;
    }

    @Override
    public double getAvailableBalance() {
        return getBalance() + overdraftLimit;
    }
}
