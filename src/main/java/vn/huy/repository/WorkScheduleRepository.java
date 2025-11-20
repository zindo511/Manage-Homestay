package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huy.model.WorkSchedule;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
}
