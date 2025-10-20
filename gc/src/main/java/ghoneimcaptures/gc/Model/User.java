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
    @Column(name = "name")
    @NotBlank(message = "Name is required")
    private String name;
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
    // @Column(name = "status")
    // @NotBlank(message = "Status is required")
    // private String status;
    // @Column(name = "token")
    // @NotBlank(message = "Token is required")
    // private String token;

}
