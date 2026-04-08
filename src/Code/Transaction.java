package Code;
// Transaction.java
import java.time.LocalDateTime;

public class Transaction {
    public static final String TYPE_DEPOSIT = "deposit";
    public static final String TYPE_WITHDRAWAL = "withdrawal";
    public static final String TYPE_TRANSFER_IN = "transfer_in";
    public static final String TYPE_TRANSFER_OUT = "transfer_out";

    private int id;
    private int accountId;
    private String type;
    private double amount;
    private String description;
    private String referenceId;  // Unique reference for each transaction
    private LocalDateTime timestamp;
    private double balanceAfter;

    public Transaction() {}
    public Transaction(int accountId, String type, double amount, String description, double balanceAfter) {
        this(accountId, type, amount, description, null, balanceAfter);
    }
    public Transaction(int accountId, String type, double amount, String description, String referenceId, double balanceAfter) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.referenceId = referenceId;
        this.timestamp = LocalDateTime.now();
        this.balanceAfter = balanceAfter;
    }

    // Getters and setters (example)
    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }
    public int getAccountId() { 
        return accountId; 
    }
    public void setAccountId(int accountid) { 
        this.accountId = accountid; 
    }
    public String getType() { 
        return type; 
    }
    public void setType(String type) { 
        this.type = type; 
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
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; 
    }
    public double getBalanceAfter() { 
        return balanceAfter; 
    }
    public void setBalanceAfter(double balanceafter) { 
        this.balanceAfter = balanceafter; 
    }
}
