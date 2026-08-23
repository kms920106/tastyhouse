package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.domain.product.model.ProductFeedbackType;

public interface ProductFeedbackJpaRepository extends JpaRepository<ProductFeedbackJpaEntity, Long> {

    /** 중복 제보 판정 — 같은 회원·메뉴·유형으로 {@code since} 이후 접수분이 있는지. */
    boolean existsByMemberIdAndProductIdAndFeedbackTypeAndCreatedAtAfter(
        Long memberId,
        Long productId,
        ProductFeedbackType feedbackType,
        LocalDateTime since
    );

    /** 빨간 점 판정 — 이 가게에 {@code since} 이후 접수분이 있는지. */
    boolean existsByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime since);
}
