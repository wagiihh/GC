package ghoneimcaptures.gc.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ghoneimcaptures.gc.Model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
