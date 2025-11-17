package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.huy.common.RoomStatus;
import vn.huy.common.RoomType;
import vn.huy.model.Room;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query(value = "select r from Room r where (:status is null or r.status = :status) " +
            "and (:capacity is null or r.capacity >= :capacity) " +
            "and (:minPrice is null or r.price >= :minPrice) " +
            "and (:maxPrice is null or r.price <= :maxPrice) " +
            "and (:type is null or r.type = :type)")
    List<Room> findRoomsByFilters (
            @Param("type") RoomType type,
            @Param("status") RoomStatus status,
            @Param("capacity") Integer capacity,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
            );

    boolean existsByName(String name);

}

