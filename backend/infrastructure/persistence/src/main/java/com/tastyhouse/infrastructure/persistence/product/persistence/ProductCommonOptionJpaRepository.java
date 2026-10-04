package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductCommonOptionJpaRepository extends JpaRepository<ProductCommonOptionJpaEntity, Long> {

    List<ProductCommonOptionJpaEntity> findAllByIdIn(List<Long> ids);

    List<ProductCommonOptionJpaEntity> findAllByOptionGroupId(Long optionGroupId);

    List<ProductCommonOptionJpaEntity> findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqual(
        LocalDateTime baseTime
    );
}
