package ghoneimcaptures.gc.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletResponse;

import ghoneimcaptures.gc.Model.Category;
import ghoneimcaptures.gc.Model.Shoot;
import ghoneimcaptures.gc.Repositories.CategoryRepository;
import ghoneimcaptures.gc.Repositories.ShootRepository;
import ghoneimcaptures.gc.Service.S3Service;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.InputStreamResource;

@RestController
@RequestMapping("/landing")
public class LandingController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShootRepository shootRepository;
    
    @Autowired
    private S3Service s3Service;
    
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    
    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;
    
    /**
     * Download file proxy endpoint - avoids popup blockers by serving from same origin
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(
            @RequestParam("url") String fileUrl,
            @RequestParam("filename") String filename) {
        
        System.out.println("=== Download Request ===");
        System.out.println("File URL: " + fileUrl);
        System.out.println("Filename: " + filename);
        
        try {
            // Extract S3 key from CloudFront URL
            String s3Key = extractS3KeyFromUrl(fileUrl);
            System.out.println("Extracted S3 Key: " + s3Key);
            
            if (s3Key == null || s3Key.isEmpty()) {
                System.err.println("Failed to extract S3 key from URL: " + fileUrl);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file URL: Could not extract S3 key");
            }
            
            // Get file stream from S3
            System.out.println("Attempting to download from S3: " + bucketName + "/" + s3Key);
            InputStream fileStream = s3Service.downloadFileAsStream(bucketName, s3Key);
            System.out.println("Successfully retrieved file stream from S3");
            
            // Determine content type from filename
            String contentType = determineContentType(filename);
            System.out.println("Content Type: " + contentType);
            
            // Encode filename for download (handle special characters and UTF-8)
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replace("+", "%20"); // Replace + with %20 for better compatibility
            
            // Set Content-Disposition header with proper encoding
            // Format: attachment; filename="name"; filename*=UTF-8''encoded-name
            String contentDisposition = String.format(
                "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                filename.replace("\"", "\\\""), // Escape quotes in filename
                encodedFilename
            );
            
            System.out.println("Content-Disposition header: " + contentDisposition);
            
            // Return streaming response with proper headers
            InputStreamResource resource = new InputStreamResource(fileStream);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
                
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error downloading file: " + e.getMessage());
        }
    }
    
    /**
     * Extract S3 key from CloudFront URL
     */
    private String extractS3KeyFromUrl(String url) {
        try {
            System.out.println("Extracting S3 key from URL: " + url);
            System.out.println("CloudFront domain: " + cloudFrontDomain);
            
            // CloudFront URL format: https://d2g86pkmykvhwc.cloudfront.net/shoots/123/images/uuid.jpg
            // We need to extract: shoots/123/images/uuid.jpg
            
            // Remove protocol if present
            String cleanUrl = url;
            if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                cleanUrl = cleanUrl.substring(cleanUrl.indexOf("://") + 3);
                System.out.println("Cleaned URL (after protocol): " + cleanUrl);
            }
            
            // Extract path after domain
            if (cleanUrl.contains(cloudFrontDomain)) {
                int domainIndex = cleanUrl.indexOf(cloudFrontDomain);
                if (domainIndex != -1) {
                    String path = cleanUrl.substring(domainIndex + cloudFrontDomain.length());
                    System.out.println("Path after domain: " + path);
                    // Remove leading slash if present
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    // Remove query parameters if present
                    if (path.contains("?")) {
                        path = path.substring(0, path.indexOf("?"));
                    }
                    // Remove hash if present
                    if (path.contains("#")) {
                        path = path.substring(0, path.indexOf("#"));
                    }
                    if (!path.isEmpty()) {
                        System.out.println("Extracted S3 key: " + path);
                        return path;
                    }
                }
            }
            
            // Fallback: try to extract from any URL structure containing /shoots/
            if (cleanUrl.contains("/shoots/")) {
                int shootsIndex = cleanUrl.indexOf("/shoots/");
                String path = cleanUrl.substring(shootsIndex + 1);
                System.out.println("Fallback path (after /shoots/): " + path);
                // Remove query parameters if present
                if (path.contains("?")) {
                    path = path.substring(0, path.indexOf("?"));
                }
                // Remove hash if present
                if (path.contains("#")) {
                    path = path.substring(0, path.indexOf("#"));
                }
                if (!path.isEmpty()) {
                    System.out.println("Extracted S3 key (fallback): " + path);
                    return path;
                }
            }
            
            System.err.println("Could not extract S3 key from URL: " + url);
            System.err.println("CloudFront domain searched: " + cloudFrontDomain);
            return null;
        } catch (Exception e) {
            System.err.println("Error extracting S3 key from URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Determine content type from filename
     */
    private String determineContentType(String filename) {
        String extension = "";
        if (filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "mp4":
                return "video/mp4";
            case "mov":
                return "video/quicktime";
            case "avi":
                return "video/x-msvideo";
            default:
                return "application/octet-stream";
        }
    }
    
    @GetMapping("/")
    public ModelAndView index(HttpServletResponse response) {
        // Set cache headers for landing page (cache for 1 hour, revalidate)
        response.setHeader("Cache-Control", "public, max-age=3600, must-revalidate");
        response.setHeader("ETag", "\"landing-page-v1\"");
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("LandingPage.html");
        return modelAndView;
    }
    @GetMapping("/projects")
    @Transactional(readOnly = true)
    public ModelAndView projects(HttpServletResponse response) {
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
        
        // Set cache headers for projects page (cache for 30 minutes, revalidate)
        // Content changes when categories are added/updated, so shorter cache
        response.setHeader("Cache-Control", "public, max-age=1800, must-revalidate");
        response.setHeader("ETag", "\"projects-v1\"");
        
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
                System.out.println("Category name: " + category.getName());
                System.out.println("Category object: " + category);
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
    // Contact endpoint moved to ContactController
    @GetMapping("/about")
    public ModelAndView about(HttpServletResponse response) {
        // Set cache headers for about page (cache for 1 hour, revalidate)
        response.setHeader("Cache-Control", "public, max-age=3600, must-revalidate");
        response.setHeader("ETag", "\"about-page-v1\"");
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("About.html");
        return modelAndView;
    }

    @GetMapping("/services")
    public ModelAndView services() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Services.html");
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
