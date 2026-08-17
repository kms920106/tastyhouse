package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    List<ProductJpaEntity> findAllByShopIdAndIdIn(Long shopId, List<Long> ids);

    long countByShopIdAndVisibleTrue(Long shopId);

    long countByShopIdAndVisibleTrueAndRepresentativeTrue(Long shopId);

    List<ProductJpaEntity> findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqual(
        LocalDateTime baseTime
    );
}
