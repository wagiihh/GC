package ghoneimcaptures.gc.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    @NotBlank(message = "Name is required")
    private String name;
    @Column(name = "url")
    @NotBlank(message = "URL is required")
    private String url;
    
    // Many-to-One relationship with Shoot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoot_id")
    private Shoot shoot;
    
    // Constructors
    public Image() {
    }
    
    public Image(String name, String url, Shoot shoot) {
        this.name = name;
        this.url = url;
        this.shoot = shoot;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Shoot getShoot() {
        return shoot;
    }
    
    public void setShoot(Shoot shoot) {
        this.shoot = shoot;
    }
}
