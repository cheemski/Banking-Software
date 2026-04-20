package Code;

public class WithdrawTransaction extends Transaction {
    private double withdrawalFee;

    public WithdrawTransaction() {}

    public WithdrawTransaction(Account account, double amount, String description, String referenceId, double balanceAfter, double withdrawalFee) {
        super(account, amount, description, referenceId, balanceAfter);
        this.withdrawalFee = withdrawalFee;
    }

    public double getWithdrawalFee() {
        return withdrawalFee;
    }

    public void setWithdrawalFee(double withdrawalFee) {
        this.withdrawalFee = withdrawalFee;
    }

    @Override
    public String getType() {
        return TYPE_WITHDRAWAL;
    }
}
