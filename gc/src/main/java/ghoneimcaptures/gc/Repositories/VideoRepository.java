package ghoneimcaptures.gc.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ghoneimcaptures.gc.Model.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {
}
