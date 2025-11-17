package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huy.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
