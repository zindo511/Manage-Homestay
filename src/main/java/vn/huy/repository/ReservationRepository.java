package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huy.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
