package Code;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base type for all ledger rows. Concrete kinds are {@link DepositTransaction},
 * {@link WithdrawTransaction}, and {@link TransferTransaction}.
 */
public abstract class Transaction {
    public static final String TYPE_DEPOSIT = "deposit";
    public static final String TYPE_WITHDRAWAL = "withdrawal";
    public static final String TYPE_TRANSFER_IN = "transfer_in";
    public static final String TYPE_TRANSFER_OUT = "transfer_out";

    private int id;
    private int accountId;
    private double amount;
    private String description;
    private String referenceId;
    private LocalDateTime timestamp;
    private double balanceAfter;

    protected Transaction() {}

    protected Transaction(int accountId, double amount, String description, String referenceId, double balanceAfter) {
        this.accountId = accountId;
        this.amount = amount;
        this.description = description;
        this.referenceId = referenceId;
        this.timestamp = LocalDateTime.now();
        this.balanceAfter = balanceAfter;
    }

    public abstract String getType();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction other = (Transaction) o;
        if (id != 0 && other.id != 0) return id == other.id;
        if (id != 0 || other.id != 0) return false;
        return accountId == other.accountId
                && Double.compare(amount, other.amount) == 0
                && Objects.equals(getType(), other.getType())
                && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(referenceId, other.referenceId);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id=" + id + ", accountId=" + accountId + ", type='" + getType() + "', amount=" + amount
                + ", description='" + description + "', referenceId='" + referenceId + "', timestamp=" + timestamp
                + ", balanceAfter=" + balanceAfter + "}";
    }
}
