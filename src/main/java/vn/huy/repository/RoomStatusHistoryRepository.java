package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.huy.model.RoomStatusHistory;

@Repository
public interface RoomStatusHistoryRepository extends JpaRepository<RoomStatusHistory, Long> {
}
