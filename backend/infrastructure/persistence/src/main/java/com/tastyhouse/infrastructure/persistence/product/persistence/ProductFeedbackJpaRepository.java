package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductFeedbackJpaRepository extends JpaRepository<ProductFeedbackJpaEntity, Long> {

    boolean existsByMemberIdAndProductIdAndFeedbackTypeAndCreatedAtAfter(
        Long memberId,
        Long productId,
        String feedbackType,
        LocalDateTime since
    );

    boolean existsByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime since);
}
