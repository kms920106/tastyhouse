package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductFeedbackReadJpaRepository extends JpaRepository<ProductFeedbackReadJpaEntity, Long> {

    Optional<ProductFeedbackReadJpaEntity> findByShopId(Long shopId);
}
