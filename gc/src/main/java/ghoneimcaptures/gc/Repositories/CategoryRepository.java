package ghoneimcaptures.gc.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ghoneimcaptures.gc.Model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
    boolean existsByName(String name);
}
