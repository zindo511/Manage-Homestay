package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.huy.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByIdentityCard(String identityCard);

    @Query(value = "select u.name from User u where u.id =:id")
    String findByNameById(@Param("id") Long id);
}
