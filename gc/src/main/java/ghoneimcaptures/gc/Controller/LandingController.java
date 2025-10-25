package ghoneimcaptures.gc.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import ghoneimcaptures.gc.Model.Category;
import ghoneimcaptures.gc.Model.Shoot;
import ghoneimcaptures.gc.Repositories.CategoryRepository;
import ghoneimcaptures.gc.Repositories.ShootRepository;
import ghoneimcaptures.gc.Service.S3Service;

import java.util.List;

@RestController
@RequestMapping("/landing")
public class LandingController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShootRepository shootRepository;
    
    @Autowired
    private S3Service s3Service;
    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("LandingPage.html");
        return modelAndView;
    }
    @GetMapping("/projects")
    @Transactional(readOnly = true)
    public ModelAndView projects() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Projects");
        
        try {
            List<Category> categories = categoryRepository.findAllWithImagesAndShoots();
            
            // Convert pre-signed URLs to public URLs if needed
            for (Category category : categories) {
                if (category.getImage() != null && category.getImage().getUrl() != null) {
                    String currentUrl = category.getImage().getUrl();
                    
                    // If URL is a pre-signed URL (contains query parameters), convert to public URL
                    if (currentUrl.contains("?")) {
                        String publicUrl = currentUrl.substring(0, currentUrl.indexOf("?"));
                        category.getImage().setUrl(publicUrl);
                        System.out.println("Converted to public URL for category: " + category.getName());
                    }
                }
            }
            
            modelAndView.addObject("categories", categories);
            System.out.println("Found " + categories.size() + " categories for projects page");
            
            // Debug: Print category and image information
            for (Category category : categories) {
                System.out.println("Category: " + category.getName() + 
                    ", Image: " + (category.getImage() != null ? category.getImage().getUrl() : "null"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            e.printStackTrace();
            modelAndView.addObject("categories", new java.util.ArrayList<>());
        }
        
        return modelAndView;
    }

    @GetMapping("/projects/{categoryId}")
    @Transactional(readOnly = true)
    public ModelAndView categoryShoots(@PathVariable Long categoryId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("CategoryShoots");
        
        try {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                // Fetch shoots with eager loading of images
                List<Shoot> shoots = shootRepository.findByCategoryIdWithImages(categoryId);
                
                // Convert pre-signed URLs to public URLs
                if (category.getImage() != null && category.getImage().getUrl() != null) {
                    String currentUrl = category.getImage().getUrl();
                    if (currentUrl.contains("?")) {
                        String publicUrl = currentUrl.substring(0, currentUrl.indexOf("?"));
                        category.getImage().setUrl(publicUrl);
                    }
                }
                
                // Convert pre-signed URLs for all images in shoots
                for (Shoot shoot : shoots) {
                    if (shoot.getImages() != null) {
                        for (ghoneimcaptures.gc.Model.Image image : shoot.getImages()) {
                            if (image.getUrl() != null && image.getUrl().contains("?")) {
                                String publicUrl = image.getUrl().substring(0, image.getUrl().indexOf("?"));
                                image.setUrl(publicUrl);
                            }
                        }
                    }
                }
                
                modelAndView.addObject("category", category);
                modelAndView.addObject("shoots", shoots);
                System.out.println("Found " + shoots.size() + " shoots for category: " + category.getName());
            } else {
                modelAndView.addObject("category", null);
                modelAndView.addObject("shoots", new java.util.ArrayList<>());
            }
        } catch (Exception e) {
            System.err.println("Error fetching shoots for category: " + e.getMessage());
            e.printStackTrace();
            modelAndView.addObject("category", null);
            modelAndView.addObject("shoots", new java.util.ArrayList<>());
        }
        
        return modelAndView;
    }
    @GetMapping("/contact")
    public ModelAndView contact() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Contact.html");
        return modelAndView;
    }
    @GetMapping("/about")
    public ModelAndView about() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("About.html");
        return modelAndView;
    }

    @GetMapping("/shoot/{shootId}")
    @Transactional(readOnly = true)
    public ModelAndView viewShoot(@PathVariable Long shootId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("ShootDetails");
        
        try {
            Shoot shoot = shootRepository.findById(shootId).orElse(null);
            if (shoot != null) {
                // Convert pre-signed URLs to public URLs for images
                if (shoot.getImages() != null) {
                    for (ghoneimcaptures.gc.Model.Image image : shoot.getImages()) {
                        if (image.getUrl() != null && image.getUrl().contains("?")) {
                            String publicUrl = image.getUrl().substring(0, image.getUrl().indexOf("?"));
                            image.setUrl(publicUrl);
                        }
                    }
                }
                
                // Convert pre-signed URLs to public URLs for videos
                if (shoot.getVideos() != null) {
                    for (ghoneimcaptures.gc.Model.Video video : shoot.getVideos()) {
                        if (video.getUrl() != null && video.getUrl().contains("?")) {
                            String publicUrl = video.getUrl().substring(0, video.getUrl().indexOf("?"));
                            video.setUrl(publicUrl);
                        }
                    }
                }
                
                modelAndView.addObject("shoot", shoot);
            } else {
                modelAndView.addObject("error", "Shoot not found");
            }
        } catch (Exception e) {
            System.err.println("Error fetching shoot: " + e.getMessage());
            e.printStackTrace();
            modelAndView.addObject("error", "Error loading shoot");
        }
        
        return modelAndView;
    }
}
