package Code;


public class TransferTransaction extends Transaction {
    private boolean incoming;

    public TransferTransaction() {}

    public TransferTransaction(Account account, double amount, String description, String referenceId, double balanceAfter, boolean incoming) {
        super(account, amount, description, referenceId, balanceAfter);
        this.incoming = incoming;
    }

    /** Fills direction when loading a row from the database. */
    void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public boolean isIncoming() {
        return incoming;
    }

    

    @Override
    public String getType() {
        return incoming ? TYPE_TRANSFER_IN : TYPE_TRANSFER_OUT;
    }
}
