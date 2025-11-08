package ghoneimcaptures.gc.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "phone", nullable = false)
    private String phone;
    
    @Column(name = "preferred_studio")
    private String studio;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "instagram_handle")
    private String instagram;
    
    @Column(name = "consent", nullable = true)
    private Boolean consent;
    
    @Column(name = "inquiry_message", columnDefinition = "TEXT")
    private String inquiryMessage;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "status")
    private String status = "NEW"; // NEW, CONTACTED, COMPLETED
    
    // Constructors
    public Contact() {
        this.submittedAt = LocalDateTime.now();
    }
    
    public Contact(String firstName, String lastName, String email, String phone, 
                   String studio, String gender, String instagram, Boolean consent) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.studio = studio;
        this.gender = gender;
        this.instagram = instagram;
        this.consent = consent;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    
    public String getStudio() {
        return studio;
    }
    
    public void setStudio(String studio) {
        this.studio = studio;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getInstagram() {
        return instagram;
    }
    
    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }
    
    public Boolean getConsent() {
        return consent;
    }
    
    public void setConsent(Boolean consent) {
        this.consent = consent;
    }
    
    public String getInquiryMessage() {
        return inquiryMessage;
    }
    
    public void setInquiryMessage(String inquiryMessage) {
        this.inquiryMessage = inquiryMessage;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", studio='" + studio + '\'' +
                ", gender='" + gender + '\'' +
                ", instagram='" + instagram + '\'' +
                ", consent=" + consent +
                ", submittedAt=" + submittedAt +
                ", status='" + status + '\'' +
                '}';
    }
}
