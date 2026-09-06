package com.tastyhouse.infrastructure.region.persistence;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminDongJpaRepository extends JpaRepository<AdminDongJpaEntity, Long> {
    Optional<AdminDongJpaEntity> findBySidoNameAndSigunguNameAndDongNameAndActiveIsTrue(
        String sidoName,
        String sigunguName,
        String dongName
    );

    boolean existsByIdAndActiveIsTrue(Long id);

    List<AdminDongJpaEntity> findByIdInAndActiveIsTrue(Collection<Long> ids);

    @Query("""
        SELECT e FROM AdminDongJpaEntity e
        WHERE e.active = true
          AND e.centerLatitude BETWEEN :minLatitude AND :maxLatitude
          AND e.centerLongitude BETWEEN :minLongitude AND :maxLongitude
        """)
    List<AdminDongJpaEntity> findAllWithinBoundingBox(
        @Param("minLatitude") BigDecimal minLatitude,
        @Param("maxLatitude") BigDecimal maxLatitude,
        @Param("minLongitude") BigDecimal minLongitude,
        @Param("maxLongitude") BigDecimal maxLongitude
    );

    @Query("SELECT e.id FROM AdminDongJpaEntity e WHERE e.active = true AND e.id IN :ids")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
}
