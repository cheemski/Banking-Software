package Code;

import java.time.LocalDateTime;

abstract class Account {
    public static final String TYPE_SAVINGS = "savings";
    public static final String TYPE_CURRENT = "current";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_FROZEN = "frozen";
    public static final String STATUS_REJECTED = "rejected";

    private int accountId;
    private User user;
    private String accountNumber;
    private String accountType;
    private double balance;
    private String status;
    private LocalDateTime createdAt;

    public Account() {}

    public Account(User user, String accountNumber, String accountType, double balance, String status) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status != null ? status : STATUS_ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getUserId() { return user != null ? user.getId() : 0; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public double getInterestRate() { return 0.0; }
    public void setInterestRate(double interestRate) {}
    public double getOverdraftLimit() { return 0.0; }
    public void setOverdraftLimit(double overdraftLimit) {}
    public double getOverdraftFee() { return 0.0; }
    public void setOverdraftFee(double overdraftFee) {}

    public double getAvailableBalance() { return balance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account other = (Account) o;
        if (accountId != 0 && other.accountId != 0) return accountId == other.accountId;
        if (accountId != 0 || other.accountId != 0) return false;
        return accountNumber != null && accountNumber.equals(other.accountNumber);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{accountId=" + accountId
                + ", userId=" + getUserId()
                + ", accountNumber='" + accountNumber + '\''
                + ", accountType='" + accountType + '\''
                + ", balance=" + balance
                + ", status='" + status + '\''
                + ", interestRate=" + getInterestRate()
                + ", overdraftLimit=" + getOverdraftLimit()
                + ", overdraftFee=" + getOverdraftFee()
                + ", createdAt=" + createdAt
                + "}";
    }
}