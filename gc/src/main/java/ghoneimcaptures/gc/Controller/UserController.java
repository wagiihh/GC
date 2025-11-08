package ghoneimcaptures.gc.Controller;

import org.mindrot.jbcrypt.BCrypt;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ghoneimcaptures.gc.Model.User;
import ghoneimcaptures.gc.Model.Contact;
import ghoneimcaptures.gc.Repositories.UserRepository;
import ghoneimcaptures.gc.Repositories.CategoryRepository;
import ghoneimcaptures.gc.Repositories.ShootRepository;
import ghoneimcaptures.gc.Repositories.ContactRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;


@Controller
@RequestMapping("/GC")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ShootRepository shootRepository;
    
    @Autowired
    private ContactRepository contactRepository;

    // Helper method to check if user is logged in
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("email") != null;
    }

    // Helper method to redirect if not logged in
    private RedirectView redirectToLogin() {
        return new RedirectView("/GC/Login");
    }

     @GetMapping("/Login")
    public ModelAndView Login() {
        ModelAndView mav = new ModelAndView("Login.html");
        return mav;
    }

    @PostMapping("/Login")
    public RedirectView loginprocess(@RequestParam ("email") String email,@RequestParam ("pass") String pass, HttpSession session) {
        User userAcc=this.userRepository.findByEmail(email);
        if(userAcc!=null)
        {
            boolean passwordsMatch = false;
            
            try {
                // Try BCrypt first (for hashed passwords)
                passwordsMatch = BCrypt.checkpw(pass, userAcc.getPassword());
            } catch (IllegalArgumentException e) {
                // If BCrypt fails, check if it's a plain text password
                if (userAcc.getPassword().equals(pass)) {
                    passwordsMatch = true;
                    // Hash the password and update it in the database
                    String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
                    userAcc.setPassword(hashedPassword);
                    userRepository.save(userAcc);
                    System.out.println("Password hashed and updated for user: " + userAcc.getEmail());
                }
            }
            
            if(passwordsMatch)
            {
                session.setAttribute("email", userAcc.getEmail());
                session.setAttribute("Firstname", userAcc.getFirstname());
                session.setAttribute("Lastname", userAcc.getLastname());
                return new RedirectView("/GC/HomePage");
            }
            else {
                return new RedirectView("/GC/Login?error=InCorrect Password" );
            }
        }
        
        return new RedirectView("/GC/Login?error=USER NOT FOUND" );
    }

    
    @GetMapping("/HomePage")
    public Object getHomePage(HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return redirectToLogin();
        }
        
        ModelAndView mav = new ModelAndView("HomePage.html");
        mav.addObject("email", (String) session.getAttribute("email"));
        mav.addObject("Firstname", (String) session.getAttribute("Firstname"));
        
        // Fetch real stats from database
        try {
            long totalUsers = userRepository.count();
            long totalCategories = categoryRepository.count();
            long totalShoots = shootRepository.count();
            long totalContacts = contactRepository.count();
            
            mav.addObject("totalUsers", totalUsers);
            mav.addObject("totalCategories", totalCategories);
            mav.addObject("totalShoots", totalShoots);
            mav.addObject("totalContacts", totalContacts);
            
            System.out.println("Dashboard stats - Users: " + totalUsers + ", Categories: " + totalCategories + ", Shoots: " + totalShoots + ", Contacts: " + totalContacts);
        } catch (Exception e) {
            System.err.println("Error fetching dashboard stats: " + e.getMessage());
            e.printStackTrace();
            // Set default values if there's an error
            mav.addObject("totalUsers", 0);
            mav.addObject("totalCategories", 0);
            mav.addObject("totalShoots", 0);
            mav.addObject("totalContacts", 0);
        }
        
        return mav;
    }


    @GetMapping("/logout")
    public RedirectView logout(HttpSession session) {
        session.invalidate();
        return new RedirectView("/GC/Login");
    }

    @GetMapping("/test-db")
    public String testDatabase() {
        try {
            long userCount = userRepository.count();
            return "Database connection successful. Total users: " + userCount;
        } catch (Exception e) {
            return "Database connection failed: " + e.getMessage();
        }
    }

    @GetMapping("/manageusers")
    public Object manageUsers(HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return redirectToLogin();
        }
        
        ModelAndView mav = new ModelAndView("manageusers.html");
        try {
            java.util.List<User> users = userRepository.findAll();
            mav.addObject("users", users);
            System.out.println("Found " + users.size() + " users");
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
            mav.addObject("users", new java.util.ArrayList<>());
        }
        return mav;
    }

    @GetMapping("/edituser/{id}")
    public Object editUser(@PathVariable Long id, HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return redirectToLogin();
        }
        
        ModelAndView mav = new ModelAndView("edituser.html");
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                mav.addObject("user", user);
                System.out.println("Editing user: " + user.getEmail());
            } else {
                System.err.println("User not found with ID: " + id);
                mav.setViewName("redirect:/GC/manageusers");
            }
        } catch (Exception e) {
            System.err.println("Error fetching user for edit: " + e.getMessage());
            e.printStackTrace();
            mav.setViewName("redirect:/GC/manageusers");
        }
        return mav;
    }

    @PostMapping("/updateuser")
    @Transactional
    public ModelAndView updateUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
        ModelAndView mav = new ModelAndView("redirect:/GC/manageusers");
        
        try {
            System.out.println("Updating user with ID: " + user.getId());
            System.out.println("User details: " + user.getEmail() + ", " + user.getFirstname() + ", " + user.getLastname());
            
            if (result.hasErrors()) {
                System.out.println("Validation errors found:");
                result.getAllErrors().forEach(error -> {
                    System.out.println("Error: " + error.getDefaultMessage());
                });
                mav.setViewName("edituser.html");
                mav.addObject("user", user);
                return mav;
            }

            // Get existing user to preserve password if new password is empty
            User existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser != null) {
                // If password is empty, keep the existing password
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    user.setPassword(existingUser.getPassword());
                } else {
                    // Hash the new password
                    String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                    user.setPassword(hashedPassword);
                }
                
                // Save the updated user
                userRepository.save(user);
                System.out.println("User updated successfully: " + user.getEmail());
            } else {
                System.err.println("User not found for update");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mav;
    }

    @GetMapping("/deleteuser/{id}")
    @Transactional
    public ModelAndView deleteUser(@PathVariable Long id, HttpSession session) {
        ModelAndView mav = new ModelAndView("redirect:/GC/manageusers");
        
        try {
            System.out.println("Deleting user with ID: " + id);
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                userRepository.deleteById(id);
                System.out.println("User deleted successfully: " + user.getEmail());
            } else {
                System.err.println("User not found for deletion");
            }
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mav;
    }

    @GetMapping("/addusers")
    public Object getaddusers(HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return redirectToLogin();
        }
        
        ModelAndView mav = new ModelAndView("addusers.html");
        User newUser=new User();
        mav.addObject("newUser",newUser);
        return mav;
    }
    @PostMapping("/addusers")
    @Transactional
    public ModelAndView addusersProcess(@Valid @ModelAttribute("newUser") User newUser, BindingResult result,
                                          @RequestParam("cpassword") String confirmPass)
    {
        ModelAndView signupModel = new ModelAndView("addusers.html");
        ModelAndView loginModel = new ModelAndView("HomePage.html");
    
        if (userRepository.existsByEmail(newUser.getEmail()))
        {
            result.rejectValue("email", "error.newUser", "Email already exists. Please choose a different email.");
        }
    
        if (!newUser.getPassword().equals(confirmPass)) {
            result.rejectValue("password", "error.newUser", "Password and Confirm Password must match.");
        }
    
        if (newUser.getPassword().length() < 8) {
            result.rejectValue("password", "error.newUser", "Password must be at least 8 characters long.");
        }
    
        if (newUser.getPassword().isEmpty()) {
            result.rejectValue("password", "error.newUser", "Password is required.");
        }
    
        // Set default role if not provided
        if (newUser.getRole() == null || newUser.getRole().isEmpty()) {
            newUser.setRole("USER");
        }
        
        // Debuggg
        System.out.println("User details before saving:");
        System.out.println("Email: " + newUser.getEmail());
        System.out.println("First Name: " + newUser.getFirstname());
        System.out.println("Last Name: " + newUser.getLastname());
        System.out.println("Role: " + newUser.getRole());
        System.out.println("Password length: " + (newUser.getPassword() != null ? newUser.getPassword().length() : "null"));
        
        // Check if user already exists
        boolean userExists = userRepository.existsByEmail(newUser.getEmail());
        System.out.println("User exists check: " + userExists);
        
        // Test database connection by counting users
        try {
            long userCount = userRepository.count();
            System.out.println("Total users in database: " + userCount);
            
            // Try to find any existing users
            if (userCount > 0) {
                System.out.println("Database has existing users, connection is working");
            } else {
                System.out.println("Database is empty but connection is working");
            }
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    
        if (result.hasErrors()) {
            System.out.println("Validation errors found:");
            result.getAllErrors().forEach(error -> {
                System.out.println("Error: " + error.getDefaultMessage());
            });
            signupModel.addObject("newUser", newUser);
            signupModel.addObject("errors", result.getAllErrors());
            return signupModel;
        } else {
            System.out.println("No validation errors, proceeding with save...");
            try {
                // Hash the password before saving
                System.out.println("Hashing password...");
                String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
                newUser.setPassword(hashedPassword);
                System.out.println("Password hashed successfully");
                
                // Save the user directly\
                newUser.setRole("USER");
                User savedUser = this.userRepository.save(newUser);
                System.out.println("User saved successfully with ID: " + savedUser.getId());
                System.out.println("Saved user email: " + savedUser.getEmail());
                
                // Verify the user was actually saved by trying to retrieve it
                User retrievedUser = this.userRepository.findByEmail(savedUser.getEmail());
                if (retrievedUser != null) {
                    System.out.println("User verification successful - user exists in database");
                } else {
                    System.out.println("WARNING: User was not found in database after saving");
                }
                
                return loginModel;
            } catch (Exception e) {
                System.err.println("Error saving user: " + e.getMessage());
                e.printStackTrace();
                result.rejectValue("email", "error.newUser", "Failed to save user. Please try again.");
                signupModel.addObject("newUser", newUser);
                signupModel.addObject("errors", result.getAllErrors());
                return signupModel;
            }
        }
                                          

    }
    
    // Contact Management Endpoints
    @GetMapping("/managecontacts")
    public Object manageContacts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false, defaultValue = "date_desc") String sortBy,
            HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return redirectToLogin();
        }
        
        ModelAndView mav = new ModelAndView("managecontacts.html");
        try {
            List<Contact> contacts = contactRepository.findAll();
            
            // Apply status filter
            if (status != null && !status.isEmpty()) {
                contacts = contacts.stream()
                    .filter(c -> c.getStatus() != null && c.getStatus().equals(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply date filter
            if (dateFilter != null && !dateFilter.isEmpty()) {
                java.time.LocalDate now = java.time.LocalDate.now();
                contacts = contacts.stream()
                    .filter(c -> {
                        if (c.getSubmittedAt() == null) return false;
                        java.time.LocalDate submittedDate = c.getSubmittedAt().toLocalDate();
                        
                        switch (dateFilter) {
                            case "today":
                                return submittedDate.equals(now);
                            case "week":
                                return submittedDate.isAfter(now.minusWeeks(1));
                            case "month":
                                return submittedDate.isAfter(now.minusMonths(1));
                            default:
                                return true;
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply sorting
            switch (sortBy) {
                case "date_asc":
                    contacts.sort((a, b) -> {
                        if (a.getSubmittedAt() == null || b.getSubmittedAt() == null) return 0;
                        return a.getSubmittedAt().compareTo(b.getSubmittedAt());
                    });
                    break;
                case "name_asc":
                    contacts.sort((a, b) -> {
                        String nameA = (a.getFirstName() + " " + a.getLastName()).toLowerCase();
                        String nameB = (b.getFirstName() + " " + b.getLastName()).toLowerCase();
                        return nameA.compareTo(nameB);
                    });
                    break;
                case "name_desc":
                    contacts.sort((a, b) -> {
                        String nameA = (a.getFirstName() + " " + a.getLastName()).toLowerCase();
                        String nameB = (b.getFirstName() + " " + b.getLastName()).toLowerCase();
                        return nameB.compareTo(nameA);
                    });
                    break;
                case "date_desc":
                default:
                    contacts.sort((a, b) -> {
                        if (a.getSubmittedAt() == null || b.getSubmittedAt() == null) return 0;
                        return b.getSubmittedAt().compareTo(a.getSubmittedAt());
                    });
                    break;
            }
            
            mav.addObject("contacts", contacts);
            mav.addObject("status", status);
            mav.addObject("dateFilter", dateFilter);
            mav.addObject("sortBy", sortBy);
            System.out.println("Found " + contacts.size() + " contact inquiries");
        } catch (Exception e) {
            System.err.println("Error fetching contacts: " + e.getMessage());
            e.printStackTrace();
            mav.addObject("contacts", new java.util.ArrayList<>());
        }
        return mav;
    }
    
    @GetMapping("/contact/mark-contacted/{id}")
    public RedirectView markContactAsContacted(@PathVariable Long id) {
        try {
            Contact contact = contactRepository.findById(id).orElse(null);
            if (contact != null) {
                contact.setStatus("CONTACTED");
                contactRepository.save(contact);
                System.out.println("Contact " + id + " marked as contacted");
            }
        } catch (Exception e) {
            System.err.println("Error marking contact as contacted: " + e.getMessage());
            e.printStackTrace();
        }
        return new RedirectView("/GC/managecontacts");
    }
    
    @GetMapping("/contact/delete/{id}")
    public RedirectView deleteContact(@PathVariable Long id) {
        try {
            contactRepository.deleteById(id);
            System.out.println("Contact " + id + " deleted");
        } catch (Exception e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            e.printStackTrace();
        }
        return new RedirectView("/GC/managecontacts");
    }
}

