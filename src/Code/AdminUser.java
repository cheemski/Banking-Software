package Code;

import java.time.LocalDate;

public class AdminUser extends User {
    private String passkey;

    public AdminUser() {
        setRole(ROLE_ADMIN);
    }

    public AdminUser(String name, String address, LocalDate dateOfBirth, String icNumber, String occupation, String email, String phone, String passwordHash, String passkey) {
        super(name, address, dateOfBirth, icNumber, occupation, email, phone, passwordHash, ROLE_ADMIN);
        this.passkey = passkey;
    }

    @Override
    public String getPasskey() {
        return passkey;
    }

    @Override
    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }
}
