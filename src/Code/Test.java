package Code;
import java.time.LocalDate;

public class Test {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        db.createTables();

        String fakeIc = "FAKE0001";
        String fakeEmail = "fakeuser@bank.com";
        String fakePassword = "password123";

        User existing = db.getUserByIc(fakeIc);
        if (existing == null) {
            User fakeUser = new User(
                    "Fake User",
                    "123 Fake St",
                    LocalDate.of(2000, 1, 1),
                    fakeIc,
                    "Tester",
                    fakeEmail,
                    "1234567890",
                    fakePassword,
                    User.ROLE_CUSTOMER
            );
            db.insertUser(fakeUser);

            User created = db.getUserByIc(fakeIc);
            if (created != null) {
                Account fakeAccount = new Savings(
                        created.getId(),
                        "ACCFAKE0001",
                        1000.0,
                        0.05,
                        Account.STATUS_ACTIVE
                );
                db.insertAccount(fakeAccount);
            }

            System.out.println("Fake user created.");
        } else {
            System.out.println("Fake user already exists.");
        }

        System.out.println("Login credentials:");
        System.out.println("IC: " + fakeIc);
        System.out.println("Email: " + fakeEmail);
        System.out.println("Password: " + fakePassword);
    }

}
