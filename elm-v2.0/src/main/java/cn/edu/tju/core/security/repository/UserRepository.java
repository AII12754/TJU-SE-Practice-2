package cn.edu.tju.core.security.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import cn.edu.tju.core.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "authorities")
    Optional<User> getUserWithAuthoritiesByUsername(String username);

    Optional<User> getUserByUsername(String username);
}
