package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huy.model.Role;

public interface RoleRepository extends JpaRepository<Role,Long> {
}
