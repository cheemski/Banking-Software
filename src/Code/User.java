package Code;
// User.java
import java.time.LocalDate;
import java.util.Objects;

public class User {
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_ADMIN = "admin";

    private int id;
    private String name;
    private String address;
    private LocalDate dateOfBirth;
    private String icNumber;
    private String occupation;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;  // "customer" or "admin"

    public User() {}
    public User(String name, String address, LocalDate dateOfBirth, String icNumber, String occupation, String email, String phone, String passwordHash) {
        this(name, address, dateOfBirth, icNumber, occupation, email, phone, passwordHash, ROLE_CUSTOMER);
    }
    public User(String name, String address, LocalDate dateOfBirth, String icNumber, String occupation, String email, String phone, String passwordHash, String role) {
        this.name = name;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.icNumber = icNumber;
        this.occupation = occupation;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : ROLE_CUSTOMER;
    }

    // Getters and setters (example for id and name)
    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }
    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }
    public String getAddress() { 
        return address; 
    }
    public void setAddress(String address) { 
        this.address = address; 
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateofbirth) { 
        this.dateOfBirth = dateofbirth; 
    }
    public String getIcNumber()  {
        return icNumber;
    }
    public void setIcNumber(String icnumber) { 
        this.icNumber = icnumber;
    }
    public String getOccupation() {
        return occupation;
    }
    public void setOccupation(String occupation) { 
        this.occupation = occupation;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) { 
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) { 
        this.phone = phone;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordhash) { 
        this.passwordHash = passwordhash; 
    }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAdmin() { return ROLE_ADMIN.equals(role); }
    public String getPasskey() { return null; }
    public void setPasskey(String passkey) {}
    public String getAlldetail() {
        return(name + "\n" + address + "\n" + dateOfBirth + "\n" + occupation + "\n" + email + "\n" + phone + "\n" + passwordHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        if (id != 0 && other.id != 0) return id == other.id;
        if (id != 0 || other.id != 0) return false;
        return icNumber != null && icNumber.equals(other.icNumber);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', icNumber='" + icNumber + "', role='" + role + "'}";
    }
}