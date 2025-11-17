package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huy.model.ServiceGroup;

public interface ServiceGroupRepository extends JpaRepository<ServiceGroup, Long> {
    boolean existsByNameContainingIgnoreCase(String name);
}
