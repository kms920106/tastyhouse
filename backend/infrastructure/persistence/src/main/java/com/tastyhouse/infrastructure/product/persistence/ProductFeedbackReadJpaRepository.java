package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFeedbackReadJpaRepository extends JpaRepository<ProductFeedbackReadJpaEntity, Long> {

    Optional<ProductFeedbackReadJpaEntity> findByShopId(Long shopId);
}
