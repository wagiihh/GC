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
import org.springframework.web.servlet.ModelAndView;

import ghoneimcaptures.gc.Model.Category;
import ghoneimcaptures.gc.Repositories.CategoryRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/GC")
public class CategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;

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
    public ModelAndView addCategoryProcess(@Valid @ModelAttribute("category") Category category, BindingResult result) {
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
                // Save the category
                Category savedCategory = categoryRepository.save(category);
                System.out.println("Category saved successfully with ID: " + savedCategory.getId());
                System.out.println("Saved category name: " + savedCategory.getName());
                
                // Verify the category was actually saved
                Category retrievedCategory = categoryRepository.findById(savedCategory.getId()).orElse(null);
                if (retrievedCategory != null) {
                    System.out.println("Category verification successful - category exists in database");
                } else {
                    System.out.println("WARNING: Category was not found in database after saving");
                }
                
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
    public ModelAndView updateCategory(@Valid @ModelAttribute("category") Category category, BindingResult result) {
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
            
            // Save the updated category
            categoryRepository.save(category);
            System.out.println("Category updated successfully: " + category.getName());
            
        } catch (Exception e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mav;
    }

    @GetMapping("/deletecategory/{id}")
    @Transactional
    public ModelAndView deleteCategory(@PathVariable Long id, HttpSession session) {
        ModelAndView mav = new ModelAndView("redirect:/GC/managecategories");
        
        try {
            System.out.println("Deleting category with ID: " + id);
            Category category = categoryRepository.findById(id).orElse(null);
            if (category != null) {
                // Check if category has associated shoots
                if (category.getShoots() != null && !category.getShoots().isEmpty()) {
                    System.out.println("Warning: Category has " + category.getShoots().size() + " associated shoots");
                    // You might want to handle this case differently - either prevent deletion or cascade delete
                }
                
                categoryRepository.deleteById(id);
                System.out.println("Category deleted successfully: " + category.getName());
            } else {
                System.err.println("Category not found for deletion");
            }
        } catch (Exception e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mav;
    }
}
