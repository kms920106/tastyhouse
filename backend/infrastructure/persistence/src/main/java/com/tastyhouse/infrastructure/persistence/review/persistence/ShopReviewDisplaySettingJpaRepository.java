package com.tastyhouse.infrastructure.persistence.review.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopReviewDisplaySettingJpaRepository extends JpaRepository<ShopReviewDisplaySettingJpaEntity, Long> {

    Optional<ShopReviewDisplaySettingJpaEntity> findByShopId(Long shopId);
}
