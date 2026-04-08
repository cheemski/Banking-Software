package Code;

import java.util.List;

/**
 * Bank administrator operations: approve/reject new accounts, set interest rates, freeze/unfreeze accounts.
 */
public class AdminService {
    private final DatabaseManager db;

    public AdminService(DatabaseManager db) {
        this.db = db;
    }

    public List<Account> listPendingAccounts() {
        return db.getAccountsByStatus(Account.STATUS_PENDING);
    }

    public boolean approveAccount(int accountId) {
        Account acc = db.getAccountById(accountId);
        if (acc == null || !Account.STATUS_PENDING.equals(acc.getStatus())) return false;
        db.updateAccountStatus(accountId, Account.STATUS_ACTIVE);
        return true;
    }

    public boolean rejectAccount(int accountId) {
        Account acc = db.getAccountById(accountId);
        if (acc == null || !Account.STATUS_PENDING.equals(acc.getStatus())) return false;
        db.updateAccountStatus(accountId, Account.STATUS_REJECTED);
        return true;
    }

    public boolean setInterestRate(int accountId, double rate) {
        Account acc = db.getAccountById(accountId);
        if (!(acc instanceof Savings)) return false;
        db.updateAccountInterestRate(accountId, rate);
        return true;
    }

    public boolean freezeAccount(int accountId) {
        Account acc = db.getAccountById(accountId);
        if (acc == null) return false;
        db.updateAccountStatus(accountId, Account.STATUS_FROZEN);
        return true;
    }

    public boolean unfreezeAccount(int accountId) {
        Account acc = db.getAccountById(accountId);
        if (acc == null) return false;
        if (!Account.STATUS_FROZEN.equals(acc.getStatus())) return false;
        db.updateAccountStatus(accountId, Account.STATUS_ACTIVE);
        return true;
    }

}
