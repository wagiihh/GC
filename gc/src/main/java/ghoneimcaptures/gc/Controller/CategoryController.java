package ghoneimcaptures.gc.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ghoneimcaptures.gc.Model.Category;
import ghoneimcaptures.gc.Model.Image;
import ghoneimcaptures.gc.Repositories.CategoryRepository;
import ghoneimcaptures.gc.Service.S3Service;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/GC")
public class CategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private S3Service s3Service;
    
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    
    @Value("${aws.region}")
    private String region;
    
    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @GetMapping("/managecategories")
    public ModelAndView manageCategories(HttpSession session) {
        ModelAndView mav = new ModelAndView("managecategories.html");
        try {
            java.util.List<Category> categories = categoryRepository.findAll();
            mav.addObject("categories", categories);
            System.out.println("Found " + categories.size() + " categories");
        } catch (Exception e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            e.printStackTrace();
            mav.addObject("categories", new java.util.ArrayList<>());
        }
        return mav;
    }

    @GetMapping("/addcategory")
    public ModelAndView getAddCategory(HttpSession session) {
        ModelAndView mav = new ModelAndView("addcategory.html");
        Category newCategory = new Category();
        mav.addObject("category", newCategory);
        return mav;
    }

    @PostMapping("/addcategory")
    @Transactional
    public ModelAndView addCategoryProcess(
            @Valid @ModelAttribute("category") Category category, 
            BindingResult result,
            @RequestParam(value = "categoryImage", required = false) MultipartFile categoryImage,
            RedirectAttributes redirectAttributes) {
        ModelAndView manageModel = new ModelAndView("redirect:/GC/managecategories");
        ModelAndView addModel = new ModelAndView("addcategory.html");

        // Check if category name already exists
        if (categoryRepository.existsByName(category.getName())) {
            result.rejectValue("name", "error.category", "Category name already exists. Please choose a different name.");
        }

        // Debug logging
        System.out.println("Category details before saving:");
        System.out.println("Name: " + category.getName());

        if (result.hasErrors()) {
            System.out.println("Validation errors found:");
            result.getAllErrors().forEach(error -> {
                System.out.println("Error: " + error.getDefaultMessage());
            });
            addModel.addObject("category", category);
            addModel.addObject("errors", result.getAllErrors());
            return addModel;
        } else {
            try {
                // Handle image upload if provided
                if (categoryImage != null && !categoryImage.isEmpty()) {
                    try {
                        // Upload image to S3
                        String fileName = "category-" + UUID.randomUUID().toString() + "-" + categoryImage.getOriginalFilename();
                        String folderName = "categories";
                        
                        // Create temporary file
                        File tempFile = File.createTempFile("category-", categoryImage.getOriginalFilename());
                        categoryImage.transferTo(tempFile);
                        
                        // Upload to S3
                        String s3Key = folderName + "/" + fileName;
                        System.out.println("Uploading to S3 - Bucket: " + bucketName + ", Key: " + s3Key + ", Region: " + region);
                        s3Service.uploadFile(bucketName, s3Key, tempFile);
                        
                        // Generate CloudFront URL
                        String s3Url = "https://" + cloudFrontDomain + "/" + s3Key;
                        
                        // Create Image entity
                        Image image = new Image(categoryImage.getOriginalFilename(), s3Url);
                        image.setCategory(category); // Set the back-reference
                        
                        // Set image to category
                        category.setImage(image);
                        
                        // Clean up temporary file
                        tempFile.delete();
                        
                        System.out.println("Category image uploaded to S3: " + s3Url);
                        System.out.println("Image entity created - Name: " + image.getName() + ", URL: " + image.getUrl());
                        
                    } catch (IOException e) {
                        System.err.println("Error uploading category image: " + e.getMessage());
                        e.printStackTrace();
                        redirectAttributes.addFlashAttribute("error", "Failed to upload category image. Please try again.");
                        addModel.addObject("category", category);
                        return addModel;
                    }
                }
                
                // Save the category
                Category savedCategory = categoryRepository.save(category);
                System.out.println("Category saved successfully with ID: " + savedCategory.getId());
                System.out.println("Saved category name: " + savedCategory.getName());
                System.out.println("Saved category image: " + (savedCategory.getImage() != null ? savedCategory.getImage().getUrl() : "null"));
                
                // Verify the category was actually saved
                Category retrievedCategory = categoryRepository.findById(savedCategory.getId()).orElse(null);
                if (retrievedCategory != null) {
                    System.out.println("Category verification successful - category exists in database");
                } else {
                    System.out.println("WARNING: Category was not found in database after saving");
                }
                
                redirectAttributes.addFlashAttribute("success", "Category created successfully!");
                return manageModel;
                
            } catch (Exception e) {
                System.err.println("Error saving category: " + e.getMessage());
                e.printStackTrace();
                result.rejectValue("name", "error.category", "Failed to save category. Please try again.");
                addModel.addObject("category", category);
                addModel.addObject("errors", result.getAllErrors());
                return addModel;
            }
        }
    }

    @GetMapping("/editcategory/{id}")
    public ModelAndView editCategory(@PathVariable Long id, HttpSession session) {
        ModelAndView mav = new ModelAndView("editcategory.html");
        try {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category != null) {
                mav.addObject("category", category);
                System.out.println("Editing category: " + category.getName());
            } else {
                System.err.println("Category not found with ID: " + id);
                mav.setViewName("redirect:/GC/managecategories");
            }
        } catch (Exception e) {
            System.err.println("Error fetching category for edit: " + e.getMessage());
            e.printStackTrace();
            mav.setViewName("redirect:/GC/managecategories");
        }
        return mav;
    }

    @PostMapping("/updatecategory")
    @Transactional
    public ModelAndView updateCategory(
            @Valid @ModelAttribute("category") Category category, 
            BindingResult result,
            @RequestParam(value = "categoryImage", required = false) MultipartFile categoryImage,
            RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView("redirect:/GC/managecategories");
        
        try {
            System.out.println("Updating category with ID: " + category.getId());
            System.out.println("Category details: " + category.getName());
            
            if (result.hasErrors()) {
                System.out.println("Validation errors found:");
                result.getAllErrors().forEach(error -> {
                    System.out.println("Error: " + error.getDefaultMessage());
                });
                mav.setViewName("editcategory.html");
                mav.addObject("category", category);
                return mav;
            }

            // Check if another category with the same name exists (excluding current category)
            Category existingCategory = categoryRepository.findByName(category.getName());
            if (existingCategory != null && !existingCategory.getId().equals(category.getId())) {
                result.rejectValue("name", "error.category", "Category name already exists. Please choose a different name.");
                mav.setViewName("editcategory.html");
                mav.addObject("category", category);
                return mav;
            }
            
            // Handle image upload if provided
            if (categoryImage != null && !categoryImage.isEmpty()) {
                try {
                    // Upload new image to S3
                    String fileName = "category-" + UUID.randomUUID().toString() + "-" + categoryImage.getOriginalFilename();
                    String folderName = "categories";
                    
                    // Create temporary file
                    File tempFile = File.createTempFile("category-", categoryImage.getOriginalFilename());
                    categoryImage.transferTo(tempFile);
                    
                    // Upload to S3
                    String s3Key = folderName + "/" + fileName;
                    s3Service.uploadFile(bucketName, s3Key, tempFile);
                    
                    // Generate CloudFront URL
                    String s3Url = "https://" + cloudFrontDomain + "/" + s3Key;
                    
                    // Create new Image entity
                    Image image = new Image(categoryImage.getOriginalFilename(), s3Url);
                    image.setCategory(category); // Set the back-reference
                    
                    // Set image to category
                    category.setImage(image);
                    
                    // Clean up temporary file
                    tempFile.delete();
                    
                    System.out.println("Category image updated in S3: " + s3Url);
                    
                } catch (IOException e) {
                    System.err.println("Error uploading category image: " + e.getMessage());
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Failed to upload category image. Please try again.");
                    mav.setViewName("editcategory.html");
                    mav.addObject("category", category);
                    return mav;
                }
            }
            
            // Save the updated category
            categoryRepository.save(category);
            System.out.println("Category updated successfully: " + category.getName());
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
            
        } catch (Exception e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update category. Please try again.");
        }
        
        return mav;
    }

    @GetMapping("/deletecategory/{id}")
    @Transactional
    public ModelAndView deleteCategory(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView("redirect:/GC/managecategories");
        
        try {
            System.out.println("Deleting category with ID: " + id);
            Category category = categoryRepository.findById(id).orElse(null);
            if (category != null) {
                // Check if category has associated shoots
                if (category.getShoots() != null && !category.getShoots().isEmpty()) {
                    System.out.println("Warning: Category has " + category.getShoots().size() + " associated shoots");
                    redirectAttributes.addFlashAttribute("warning", "Category has associated shoots. Please delete shoots first.");
                    return mav;
                }
                
                // Delete associated image from S3 if exists
                if (category.getImage() != null && category.getImage().getUrl() != null) {
                    try {
                        // Extract file path from S3 URL for deletion
                        String imageUrl = category.getImage().getUrl();
                        String s3Key = imageUrl.substring(imageUrl.indexOf(bucketName) + bucketName.length() + 1);
                        s3Service.deleteFile(bucketName, s3Key);
                        System.out.println("Category image deleted from S3: " + s3Key);
                    } catch (Exception e) {
                        System.err.println("Error deleting category image from S3: " + e.getMessage());
                        // Continue with category deletion even if image deletion fails
                    }
                }
                
                categoryRepository.deleteById(id);
                System.out.println("Category deleted successfully: " + category.getName());
                redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
            } else {
                System.err.println("Category not found for deletion");
                redirectAttributes.addFlashAttribute("error", "Category not found.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete category. Please try again.");
        }
        
        return mav;
    }
}
