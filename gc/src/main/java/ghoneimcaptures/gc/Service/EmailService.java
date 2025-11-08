package ghoneimcaptures.gc.Service;

import ghoneimcaptures.gc.Model.User;
import ghoneimcaptures.gc.Model.Contact;
import ghoneimcaptures.gc.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${app.email.from:noreply@ghoneimcaptures.com}")
    private String fromEmail;
    
    @Async
    public void sendContactNotification(Contact contact) {
        try {
            // Get all users from the users table
            List<User> allUsers = userRepository.findAll();
            
            if (allUsers.isEmpty()) {
                System.err.println("No users found in the database to send notification to");
                return;
            }
            
            // Create email content
            String subject = "New Contact Form Submission - " + contact.getFullName();
            String message = buildContactNotificationMessage(contact);
            
            // Send email to all users
            for (User user : allUsers) {
                String htmlMessage = buildContactNotificationHtml(contact);
                sendHtmlEmail(user.getEmail(), subject, htmlMessage);
                System.out.println("Contact notification sent to: " + user.getEmail());
            }
            
        } catch (Exception e) {
            System.err.println("Error sending contact notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom(fromEmail);
            
            mailSender.send(mailMessage);
            System.out.println("Email sent successfully to: " + to);
            
        } catch (Exception e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            
            mailSender.send(mimeMessage);
            System.out.println("HTML email sent successfully to: " + to);
            
        } catch (MessagingException e) {
            System.err.println("Error sending HTML email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String buildContactNotificationMessage(Contact contact) {
        StringBuilder message = new StringBuilder();
        
        message.append("New contact form submission received!\n\n");
        message.append("Contact Details:\n");
        message.append("================\n");
        message.append("Name: ").append(contact.getFullName()).append("\n");
        message.append("Email: ").append(contact.getEmail()).append("\n");
        message.append("Phone: ").append(contact.getPhone()).append("\n");
        
        if (contact.getInstagram() != null && !contact.getInstagram().isEmpty()) {
            message.append("Instagram: ").append(contact.getInstagram()).append("\n");
        }
        
        message.append("Submitted At: ").append(contact.getSubmittedAt()).append("\n");
        message.append("Status: ").append(contact.getStatus()).append("\n\n");
        
        message.append("Please follow up with this potential client as soon as possible.\n\n");
        message.append("Best regards,\n");
        message.append("Ghoneim Captures System");
        
        return message.toString();
    }
    
    @Async
    public void sendWelcomeEmail(Contact contact) {
        try {
            String subject = "Thank you for contacting Ghoneim Captures!";
            String htmlMessage = buildWelcomeHtml(contact);
            
            sendHtmlEmail(contact.getEmail(), subject, htmlMessage);
            System.out.println("Welcome email sent to: " + contact.getEmail());
            
        } catch (Exception e) {
            System.err.println("Error sending welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String buildWelcomeMessage(Contact contact) {
        StringBuilder message = new StringBuilder();
        
        message.append("Dear ").append(contact.getFirstName()).append(",\n\n");
        message.append("Thank you for reaching out to Ghoneim Captures!\n\n");
        message.append("We have received your inquiry and our team will review your information shortly. ");
        message.append("We typically respond within 24 hours during business days.\n\n");
        
        message.append("What happens next?\n");
        message.append("==================\n");
        message.append("1. Our team will review your information\n");
        message.append("2. We'll contact you to discuss your photography needs\n");
        message.append("3. We'll schedule a consultation if needed\n");
        message.append("4. We'll provide you with a customized quote\n\n");
        
        message.append("If you have any immediate questions, please don't hesitate to call us at 1300 288 818.\n\n");
        message.append("We look forward to working with you!\n\n");
        message.append("Best regards,\n");
        message.append("The Ghoneim Captures Team");
        
        return message.toString();
    }
    
    private String buildContactNotificationHtml(Contact contact) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        
        // Logo with fallback
        html.append("<div style='text-align: center; margin-bottom: 30px;'>");
        html.append("<img src='https://d2g86pkmykvhwc.cloudfront.net/Logo/Enclosed%20Horizontal%20Logo%20Black.png' alt='Ghoneim Captures' style='max-width: 200px; height: auto; display: block; margin: 0 auto;'>");
        html.append("<div style='display: none; font-size: 24px; font-weight: bold; color: #c0a97a; margin-top: 10px;'>GHONEIM CAPTURES</div>");
        html.append("</div>");
        
        html.append("<h2 style='color: #333;'>New Contact Form Submission</h2>");
        html.append("<p>You have received a new contact form submission:</p>");
        
        html.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        html.append("<h3 style='margin-top: 0; color: #333;'>Contact Details</h3>");
        html.append("<p><strong>Name:</strong> ").append(contact.getFullName()).append("</p>");
        html.append("<p><strong>Email:</strong> ").append(contact.getEmail()).append("</p>");
        html.append("<p><strong>Phone:</strong> ").append(contact.getPhone()).append("</p>");
        
        if (contact.getInstagram() != null && !contact.getInstagram().isEmpty()) {
            html.append("<p><strong>Instagram:</strong> ").append(contact.getInstagram()).append("</p>");
        }
        
        if (contact.getInquiryMessage() != null && !contact.getInquiryMessage().trim().isEmpty()) {
            html.append("<p><strong>Inquiry Message:</strong></p>");
            html.append("<div style='background: #ffffff; padding: 15px; border-radius: 6px; border-left: 4px solid #c0a97a; margin: 10px 0;'>");
            html.append("<p style='margin: 0; white-space: pre-wrap;'>").append(contact.getInquiryMessage()).append("</p>");
            html.append("</div>");
        }
        
        html.append("<p><strong>Submitted:</strong> ").append(contact.getSubmittedAt()).append("</p>");
        html.append("</div>");
        
        html.append("<p style='color: #666;'>Please follow up with this potential client as soon as possible.</p>");
        html.append("<p>Best regards,<br>Ghoneim Captures System</p>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }
    
    private String buildWelcomeHtml(Contact contact) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        
        // Logo with fallback
        html.append("<div style='text-align: center; margin-bottom: 30px;'>");
        html.append("<img src='https://d2g86pkmykvhwc.cloudfront.net/Logo/Enclosed%20Horizontal%20Logo%20Black.png' alt='Ghoneim Captures' style='max-width: 200px; height: auto; display: block; margin: 0 auto;'>");
        html.append("<div style='display: none; font-size: 24px; font-weight: bold; color: #c0a97a; margin-top: 10px;'>GHONEIM CAPTURES</div>");
        html.append("</div>");
        
        html.append("<h2 style='color: #333;'>Thank you for contacting Ghoneim Captures!</h2>");
        html.append("<p>Dear ").append(contact.getFirstName()).append(",</p>");
        
        html.append("<p>Thank you for reaching out to Ghoneim Captures!</p>");
        html.append("<p>We have received your inquiry and our team will review your information shortly. ");
        html.append("We typically respond within 24 hours during business days.</p>");
        
        html.append("<h3 style='color: #333;'>What happens next?</h3>");
        html.append("<ol>");
        html.append("<li>Our team will review your information</li>");
        html.append("<li>We'll contact you to discuss your photography needs</li>");
        html.append("<li>We'll schedule a consultation if needed</li>");
        html.append("<li>We'll provide you with a customized quote</li>");
        html.append("</ol>");
        
        html.append("<p>If you have any immediate questions, please don't hesitate to call us at <strong>1300 288 818</strong>.</p>");
        html.append("<p>We look forward to working with you!</p>");
        
        html.append("<p>Best regards,<br>The Ghoneim Captures Team</p>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }
}
