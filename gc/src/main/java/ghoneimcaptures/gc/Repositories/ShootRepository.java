package ghoneimcaptures.gc.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ghoneimcaptures.gc.Model.Shoot;

import java.util.List;

public interface ShootRepository extends JpaRepository<Shoot, Long> {
    List<Shoot> findByCategoryId(Long categoryId);
    
    @Query("SELECT s FROM Shoot s WHERE s.category.id = :categoryId")
    List<Shoot> findShootsByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT DISTINCT s FROM Shoot s LEFT JOIN FETCH s.images WHERE s.category.id = :categoryId")
    List<Shoot> findByCategoryIdWithImages(@Param("categoryId") Long categoryId);
}
