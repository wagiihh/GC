package ghoneimcaptures.gc.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import ghoneimcaptures.gc.Model.User;

public interface UserRepository extends JpaRepository<User,Long>{
    User findByEmail(String email);
    User findbyId(Long id);
    User findByName(String name);
}
