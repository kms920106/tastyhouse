package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductExposureHourJpaRepository extends JpaRepository<ProductExposureHourJpaEntity, Long> {
    List<ProductExposureHourJpaEntity> findAllByProductId(Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProductExposureHourJpaEntity h where h.productId = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);
}
