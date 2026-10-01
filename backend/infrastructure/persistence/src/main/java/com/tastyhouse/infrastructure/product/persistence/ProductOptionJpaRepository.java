package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOptionJpaEntity, Long> {

    List<ProductOptionJpaEntity> findAllByIdIn(List<Long> ids);

    List<ProductOptionJpaEntity> findAllByOptionGroupId(Long optionGroupId);

    List<ProductOptionJpaEntity> findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqual(
        LocalDateTime baseTime
    );
}
