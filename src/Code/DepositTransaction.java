package Code;

public class DepositTransaction extends Transaction {
    private double DepositMinimum;

    public DepositTransaction() {}

    public DepositTransaction(Account account, double amount, String description, String referenceId, double balanceAfter, double depositMinimum) {
        super(account, amount, description, referenceId, balanceAfter);
        this.DepositMinimum = depositMinimum;
    }

    public double getDepositMinimum(){
        return this.DepositMinimum;
    }

    public void setDepositMinimum(double depositMinimum){
        this.DepositMinimum = depositMinimum;
    }

    @Override
    public String getType() {
        return TYPE_DEPOSIT;
    }
}
