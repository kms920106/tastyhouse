package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.domain.product.model.ProductFeedbackType;

public interface ProductFeedbackJpaRepository extends JpaRepository<ProductFeedbackJpaEntity, Long> {
    boolean existsByMemberIdAndProductIdAndFeedbackTypeAndCreatedAtAfter(
        Long memberId,
        Long productId,
        ProductFeedbackType feedbackType,
        LocalDateTime since
    );

    boolean existsByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime since);
}
