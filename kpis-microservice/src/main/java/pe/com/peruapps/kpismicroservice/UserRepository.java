package pe.com.peruapps.kpismicroservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByEmailIgnoreCaseOrNameIgnoreCase(String email, String name);
}

