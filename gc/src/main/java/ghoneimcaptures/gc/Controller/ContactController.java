package ghoneimcaptures.gc.Controller;

import ghoneimcaptures.gc.Model.Contact;
import ghoneimcaptures.gc.Repositories.ContactRepository;
import ghoneimcaptures.gc.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/landing")
public class ContactController {
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/contact")
    public String contactForm(Model model, HttpServletResponse response) {
        // Set cache headers for contact page (cache for 1 hour, revalidate)
        response.setHeader("Cache-Control", "public, max-age=3600, must-revalidate");
        response.setHeader("ETag", "\"contact-page-v1\"");
        
        model.addAttribute("contact", new Contact());
        return "Contact.html";
    }
    
    @PostMapping("/contact")
    public String submitContactForm(@ModelAttribute Contact contact, 
                                  RedirectAttributes redirectAttributes) {
        try {
            // Validate required fields
            if (contact.getFirstName() == null || contact.getFirstName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First name is required");
                return "redirect:/landing/contact";
            }
            
            if (contact.getLastName() == null || contact.getLastName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Last name is required");
                return "redirect:/landing/contact";
            }
            
            if (contact.getEmail() == null || contact.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Email is required");
                return "redirect:/landing/contact";
            }
            
            if (contact.getPhone() == null || contact.getPhone().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Phone number is required");
                return "redirect:/landing/contact";
            }
            
            // Set default consent value since checkbox was removed
            if (contact.getConsent() == null) {
                contact.setConsent(true); // Default to true since they're submitting the form
            }
            
            // Save contact to database
            Contact savedContact = contactRepository.save(contact);
            System.out.println("Contact saved with ID: " + savedContact.getId());
            
            // Send emails asynchronously (non-blocking)
            try {
                emailService.sendContactNotification(savedContact);
                emailService.sendWelcomeEmail(savedContact);
                System.out.println("Email sending initiated asynchronously");
            } catch (Exception e) {
                System.err.println("Error initiating email sending: " + e.getMessage());
                // Don't fail the form submission if email fails
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("success", 
                "Thank you for your inquiry! We have received your information and will contact you soon.");
            
            return "redirect:/landing/contact";
            
        } catch (Exception e) {
            System.err.println("Error processing contact form: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("error", 
                "There was an error processing your request. Please try again or contact us directly.");
            
            return "redirect:/landing/contact";
        }
    }
    
    // Admin endpoint to view all contacts (optional)
    @GetMapping("/admin/contacts")
    public String viewContacts(Model model) {
        try {
            model.addAttribute("contacts", contactRepository.findRecentContacts());
            return "admin/contacts"; // You would need to create this view
        } catch (Exception e) {
            System.err.println("Error fetching contacts: " + e.getMessage());
            model.addAttribute("error", "Error loading contacts");
            return "admin/contacts";
        }
    }
    
    // Admin endpoint to update contact status
    @PostMapping("/admin/contacts/{id}/status")
    public String updateContactStatus(@PathVariable Long id, 
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            Contact contact = contactRepository.findById(id).orElse(null);
            if (contact != null) {
                contact.setStatus(status);
                contactRepository.save(contact);
                redirectAttributes.addFlashAttribute("success", "Contact status updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Contact not found");
            }
        } catch (Exception e) {
            System.err.println("Error updating contact status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error updating contact status");
        }
        
        return "redirect:/landing/admin/contacts";
    }
}
