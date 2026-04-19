package Code;

public class DepositTransaction extends Transaction {

    public DepositTransaction() {}

    public DepositTransaction(int accountId, double amount, String description, String referenceId, double balanceAfter) {
        super(accountId, amount, description, referenceId, balanceAfter);
    }

    @Override
    public String getType() {
        return TYPE_DEPOSIT;
    }
}
