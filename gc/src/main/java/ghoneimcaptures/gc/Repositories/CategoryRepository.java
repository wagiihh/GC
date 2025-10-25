package ghoneimcaptures.gc.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ghoneimcaptures.gc.Model.Category;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.image LEFT JOIN FETCH c.shoots")
    List<Category> findAllWithImagesAndShoots();
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.image WHERE c.id = :id")
    Category findByIdWithImage(@Param("id") Long id);
}
