package Code;

public class WithdrawTransaction extends Transaction {

    public WithdrawTransaction() {}

    public WithdrawTransaction(int accountId, double amount, String description, String referenceId, double balanceAfter) {
        super(accountId, amount, description, referenceId, balanceAfter);
    }

    @Override
    public String getType() {
        return TYPE_WITHDRAWAL;
    }
}
