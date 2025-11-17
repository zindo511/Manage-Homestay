package vn.huy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.huy.model.ServiceEntity;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    @Query("SELECT s FROM ServiceEntity s " +
            "WHERE (:groupId IS NULL OR s.group.id = :groupId) " +
            "AND (:isActive IS NULL OR s.isActive = :isActive) " +
            "AND (:minPrice IS NULL OR s.unitPrice >= :minPrice) " +
            "AND (:maxPrice is null or s.unitPrice <= :maxPrice)")
    List<ServiceEntity> findByFilter(
            @Param("groupId") Integer groupId,
            @Param("isActive") Boolean isActive,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    boolean existsByNameAndGroup_Id(String name, Long groupId);

}
