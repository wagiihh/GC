package ghoneimcaptures.gc.Controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ghoneimcaptures.gc.Model.Category;
import ghoneimcaptures.gc.Model.Image;
import ghoneimcaptures.gc.Model.Shoot;
import ghoneimcaptures.gc.Model.Video;
import ghoneimcaptures.gc.Repositories.CategoryRepository;
import ghoneimcaptures.gc.Repositories.ImageRepository;
import ghoneimcaptures.gc.Repositories.ShootRepository;
import ghoneimcaptures.gc.Repositories.VideoRepository;
import ghoneimcaptures.gc.Service.S3Service;

@Controller
@RequestMapping("/admin/shoots")
public class ShootController {

    @Autowired
    private ShootRepository shootRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;

    // List all shoots
    @GetMapping
    public String listShoots(Model model) {
        List<Shoot> shoots = shootRepository.findAll();
        model.addAttribute("shoots", shoots);
        return "manageshoots";
    }

    // Show add shoot form
    @GetMapping("/add")
    public String showAddShootForm(Model model) {
        model.addAttribute("shoot", new Shoot());
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "addshoot";
    }

    // Process add shoot
    @PostMapping("/add")
    @Transactional
    public String addShootProcess(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam("location") String location,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "videos", required = false) MultipartFile[] videos,
            RedirectAttributes redirectAttributes) {

        try {
            // Set category
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Category not found!");
                return "redirect:/admin/shoots/add";
            }
            
            // Create new Shoot object
            Shoot shoot = new Shoot();
            shoot.setName(name);
            shoot.setDescription(description);
            shoot.setDate(date);
            shoot.setLocation(location);
            shoot.setCategory(category);

            // Save shoot first to get ID
            Shoot savedShoot = shootRepository.save(shoot);

            // Handle images asynchronously with optimized parallel processing
            List<CompletableFuture<Image>> imageFutures = new ArrayList<>();
            if (images != null) {
                for (MultipartFile imageFile : images) {
                    if (!imageFile.isEmpty()) {
                        CompletableFuture<Image> imageFuture = CompletableFuture.supplyAsync(() -> {
                            try {
                                String originalFilename = imageFile.getOriginalFilename();
                                String extension = "";
                                if (originalFilename != null && originalFilename.contains(".")) {
                                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                                }
                                String s3Key = "shoots/" + savedShoot.getId() + "/images/" + UUID.randomUUID() + extension;

                                // Upload to S3 asynchronously with timeout
                                File tempFile = File.createTempFile("temp-", extension);
                                imageFile.transferTo(tempFile);
                                
                                // Use optimized upload with timeout
                                s3Service.uploadLargeFileAsync(bucketName, s3Key, tempFile)
                                    .orTimeout(300, java.util.concurrent.TimeUnit.SECONDS) // 5 minute timeout
                                    .join();
                                tempFile.delete();

                                // Generate CloudFront URL
                                String cloudFrontUrl = "https://" + cloudFrontDomain + "/" + s3Key;

                                // Create Image entity
                                Image image = new Image();
                                image.setName(originalFilename != null ? originalFilename : "image" + extension);
                                image.setUrl(cloudFrontUrl);
                                image.setShoot(savedShoot);

                                return image;
                            } catch (Exception e) {
                                System.err.println("Error uploading image: " + e.getMessage());
                                return null;
                            }
                        });
                        imageFutures.add(imageFuture);
                    }
                }
            }

            // Handle videos asynchronously with optimized parallel processing
            List<CompletableFuture<Video>> videoFutures = new ArrayList<>();
            if (videos != null) {
                for (MultipartFile videoFile : videos) {
                    if (!videoFile.isEmpty()) {
                        CompletableFuture<Video> videoFuture = CompletableFuture.supplyAsync(() -> {
                            try {
                                String originalFilename = videoFile.getOriginalFilename();
                                String extension = "";
                                if (originalFilename != null && originalFilename.contains(".")) {
                                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                                }
                                String s3Key = "shoots/" + savedShoot.getId() + "/videos/" + UUID.randomUUID() + extension;

                                // Upload to S3 asynchronously with timeout
                                File tempFile = File.createTempFile("temp-", extension);
                                videoFile.transferTo(tempFile);
                                
                                // Use optimized upload with timeout
                                s3Service.uploadLargeFileAsync(bucketName, s3Key, tempFile)
                                    .orTimeout(600, java.util.concurrent.TimeUnit.SECONDS) // 10 minute timeout for videos
                                    .join();
                                tempFile.delete();

                                // Generate CloudFront URL
                                String cloudFrontUrl = "https://" + cloudFrontDomain + "/" + s3Key;

                                // Create Video entity
                                Video video = new Video();
                                video.setName(originalFilename != null ? originalFilename : "video" + extension);
                                video.setUrl(cloudFrontUrl);
                                video.setShoot(savedShoot);

                                return video;
                            } catch (Exception e) {
                                System.err.println("Error uploading video: " + e.getMessage());
                                return null;
                            }
                        });
                        videoFutures.add(videoFuture);
                    }
                }
            }

            // Wait for all uploads to complete and collect results
            List<Image> imageList = new ArrayList<>();
            for (CompletableFuture<Image> future : imageFutures) {
                try {
                    Image image = future.get();
                    if (image != null) {
                        imageList.add(image);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing image upload: " + e.getMessage());
                }
            }

            List<Video> videoList = new ArrayList<>();
            for (CompletableFuture<Video> future : videoFutures) {
                try {
                    Video video = future.get();
                    if (video != null) {
                        videoList.add(video);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing video upload: " + e.getMessage());
                }
            }

            // Save all images and videos
            if (!imageList.isEmpty()) {
                imageRepository.saveAll(imageList);
            }
            if (!videoList.isEmpty()) {
                videoRepository.saveAll(videoList);
            }

            redirectAttributes.addFlashAttribute("success", "Shoot added successfully!");
            return "redirect:/admin/shoots";

        } catch (Exception e) {
            System.err.println("Error adding shoot: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error adding shoot: " + e.getMessage());
            return "redirect:/admin/shoots/add";
        }
    }

    // Show edit shoot form
    @GetMapping("/edit/{id}")
    public String showEditShootForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Shoot shoot = shootRepository.findById(id).orElse(null);
        if (shoot == null) {
            redirectAttributes.addFlashAttribute("error", "Shoot not found!");
            return "redirect:/admin/shoots";
        }

        model.addAttribute("shoot", shoot);
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "editshoot";
    }

    // Process edit shoot
    @PostMapping("/edit/{id}")
    @Transactional
    public String editShootProcess(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam("location") String location,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
            @RequestParam(value = "newVideos", required = false) MultipartFile[] newVideos,
            @RequestParam(value = "imagesToDelete", required = false) Long[] imagesToDelete,
            @RequestParam(value = "videosToDelete", required = false) Long[] videosToDelete,
            RedirectAttributes redirectAttributes) {

        try {
            // Get existing shoot
            Shoot existingShoot = shootRepository.findById(id).orElse(null);
            if (existingShoot == null) {
                redirectAttributes.addFlashAttribute("error", "Shoot not found!");
                return "redirect:/admin/shoots";
            }

            // Update basic fields
            existingShoot.setName(name);
            existingShoot.setDescription(description);
            existingShoot.setDate(date);
            existingShoot.setLocation(location);

            // Set category
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                existingShoot.setCategory(category);
            }

            // Save updated shoot
            Shoot savedShoot = shootRepository.save(existingShoot);

            // Handle image deletions
            if (imagesToDelete != null) {
                for (Long imageId : imagesToDelete) {
                    Image image = imageRepository.findById(imageId).orElse(null);
                    if (image != null) {
                        // Delete from S3
                        String url = image.getUrl();
                        if (url != null && url.contains(cloudFrontDomain)) {
                            String s3Key = url.substring(url.indexOf(cloudFrontDomain) + cloudFrontDomain.length() + 1);
                            s3Service.deleteFile(bucketName, s3Key);
                        }
                        imageRepository.delete(image);
                    }
                }
            }

            // Handle video deletions
            if (videosToDelete != null) {
                for (Long videoId : videosToDelete) {
                    Video video = videoRepository.findById(videoId).orElse(null);
                    if (video != null) {
                        // Delete from S3
                        String url = video.getUrl();
                        if (url != null && url.contains(cloudFrontDomain)) {
                            String s3Key = url.substring(url.indexOf(cloudFrontDomain) + cloudFrontDomain.length() + 1);
                            s3Service.deleteFile(bucketName, s3Key);
                        }
                        videoRepository.delete(video);
                    }
                }
            }

            // Handle new images
            List<Image> imageList = new ArrayList<>();
            if (newImages != null) {
                for (MultipartFile imageFile : newImages) {
                    if (!imageFile.isEmpty()) {
                        try {
                            String originalFilename = imageFile.getOriginalFilename();
                            String extension = "";
                            if (originalFilename != null && originalFilename.contains(".")) {
                                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                            }
                            String s3Key = "shoots/" + savedShoot.getId() + "/images/" + UUID.randomUUID() + extension;

                            // Upload to S3
                            File tempFile = File.createTempFile("temp-", extension);
                            imageFile.transferTo(tempFile);
                            s3Service.uploadFile(bucketName, s3Key, tempFile);
                            tempFile.delete();

                            // Generate CloudFront URL
                            String cloudFrontUrl = "https://" + cloudFrontDomain + "/" + s3Key;

                            // Create Image entity
                            Image image = new Image();
                            image.setName(originalFilename != null ? originalFilename : "image" + extension);
                            image.setUrl(cloudFrontUrl);
                            image.setShoot(savedShoot);

                            imageList.add(image);
                        } catch (IOException e) {
                            System.err.println("Error uploading image: " + e.getMessage());
                        }
                    }
                }
            }

            // Handle new videos
            List<Video> videoList = new ArrayList<>();
            if (newVideos != null) {
                for (MultipartFile videoFile : newVideos) {
                    if (!videoFile.isEmpty()) {
                        try {
                            String originalFilename = videoFile.getOriginalFilename();
                            String extension = "";
                            if (originalFilename != null && originalFilename.contains(".")) {
                                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                            }
                            String s3Key = "shoots/" + savedShoot.getId() + "/videos/" + UUID.randomUUID() + extension;

                            // Upload to S3
                            File tempFile = File.createTempFile("temp-", extension);
                            videoFile.transferTo(tempFile);
                            s3Service.uploadFile(bucketName, s3Key, tempFile);
                            tempFile.delete();

                            // Generate CloudFront URL
                            String cloudFrontUrl = "https://" + cloudFrontDomain + "/" + s3Key;

                            // Create Video entity
                            Video video = new Video();
                            video.setName(originalFilename != null ? originalFilename : "video" + extension);
                            video.setUrl(cloudFrontUrl);
                            video.setShoot(savedShoot);

                            videoList.add(video);
                        } catch (IOException e) {
                            System.err.println("Error uploading video: " + e.getMessage());
                        }
                    }
                }
            }

            // Save new images and videos
            if (!imageList.isEmpty()) {
                imageRepository.saveAll(imageList);
            }
            if (!videoList.isEmpty()) {
                videoRepository.saveAll(videoList);
            }

            redirectAttributes.addFlashAttribute("success", "Shoot updated successfully!");
            return "redirect:/admin/shoots";

        } catch (Exception e) {
            System.err.println("Error updating shoot: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating shoot: " + e.getMessage());
            return "redirect:/admin/shoots/edit/" + id;
        }
    }

    // Delete shoot
    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteShoot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Shoot shoot = shootRepository.findById(id).orElse(null);
            if (shoot != null) {
                // Delete images from S3
                if (shoot.getImages() != null) {
                    for (Image image : shoot.getImages()) {
                        String url = image.getUrl();
                        if (url != null && url.contains(cloudFrontDomain)) {
                            String s3Key = url.substring(url.indexOf(cloudFrontDomain) + cloudFrontDomain.length() + 1);
                            s3Service.deleteFile(bucketName, s3Key);
                        }
                    }
                }

                // Delete videos from S3
                if (shoot.getVideos() != null) {
                    for (Video video : shoot.getVideos()) {
                        String url = video.getUrl();
                        if (url != null && url.contains(cloudFrontDomain)) {
                            String s3Key = url.substring(url.indexOf(cloudFrontDomain) + cloudFrontDomain.length() + 1);
                            s3Service.deleteFile(bucketName, s3Key);
                        }
                    }
                }

                shootRepository.delete(shoot);
                redirectAttributes.addFlashAttribute("success", "Shoot deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Shoot not found!");
            }
        } catch (Exception e) {
            System.err.println("Error deleting shoot: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting shoot: " + e.getMessage());
        }

        return "redirect:/admin/shoots";
    }
}
