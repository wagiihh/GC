package ghoneimcaptures.gc.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
@Entity
@Table(name = "shoots")
public class Shoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    @NotBlank(message = "Name is required")
    private String name;
    @Column(name = "description")
    @NotBlank(message = "Description is required")
    private String description;
    @Column(name = "date")
    @NotBlank(message = "Date is required")
    private String date;
    @Column(name = "location")
    @NotBlank(message = "Location is required")
    private String location;
    
    // Many-to-One relationship with Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    // One-to-Many relationship with Images
    @OneToMany(mappedBy = "shoot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> images;
    
    // One-to-Many relationship with Videos
    @OneToMany(mappedBy = "shoot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Video> videos;
    
    // Constructors
    public Shoot() {
    }
    
    public Shoot(String name, String description, String date, String location, Category category) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.location = location;
        this.category = category;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public List<Image> getImages() {
        return images;
    }
    
    public void setImages(List<Image> images) {
        this.images = images;
    }
    
    public List<Video> getVideos() {
        return videos;
    }
    
    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
}
