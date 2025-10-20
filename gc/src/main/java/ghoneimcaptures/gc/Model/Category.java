package ghoneimcaptures.gc.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    @NotBlank(message = "Name is required")
    private String name;
    
    // One-to-Many relationship with Shoots
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Shoot> shoots;
    
    // Constructors
    public Category() {
    }
    
    public Category(String name) {
        this.name = name;
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
    
    public List<Shoot> getShoots() {
        return shoots;
    }
    
    public void setShoots(List<Shoot> shoots) {
        this.shoots = shoots;
    }
}
