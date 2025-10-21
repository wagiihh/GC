package ghoneimcaptures.gc.Model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "Firstname")
    @NotBlank(message = "Firstname is required")
    private String Firstname;
    @Column(name = "Lastname")
    @NotBlank(message = "Lastname is required")
    private String Lastname;
    @Column(name = "email")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;
    @Column(name = "password")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    @Column(name = "role")
    @NotBlank(message = "Role is required")
    private String role;
    

    public User(String email,String Fristname,String Lastname,String role)
    {
        this.email=email;
        this.Firstname=Firstname;
        this.Lastname=Lastname;
        this.role=role;
    }

    public User(){}


    public Long getId() {
        return id;
    }

    public String getFirstname() {
        return Firstname;
    }


    public void setFirstname(String firstname) {
        Firstname = firstname;
    }


    public String getLastname() {
        return Lastname;
    }


    public void setLastname(String lastname) {
        Lastname = lastname;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getRole() {
        return role;
    }


    public void setRole(String role) {
        this.role = role;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

   

}
