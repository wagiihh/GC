package ghoneimcaptures.gc.Repositories;

import ghoneimcaptures.gc.Model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    // Find contacts by status
    List<Contact> findByStatus(String status);
    
    // Find contacts submitted after a specific date
    List<Contact> findBySubmittedAtAfter(LocalDateTime date);
    
    // Find contacts by email
    List<Contact> findByEmail(String email);
    
    // Find contacts by studio preference
    List<Contact> findByStudio(String studio);
    
    // Custom query to find recent contacts
    @Query("SELECT c FROM Contact c ORDER BY c.submittedAt DESC")
    List<Contact> findRecentContacts();
    
    // Count contacts by status
    long countByStatus(String status);
    
    // Find contacts submitted today
    @Query("SELECT c FROM Contact c WHERE c.submittedAt >= CURRENT_DATE")
    List<Contact> findTodayContacts();
}
