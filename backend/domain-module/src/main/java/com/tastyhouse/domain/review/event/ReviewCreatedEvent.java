package com.tastyhouse.domain.review.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 리뷰 등록 도메인 이벤트.
 *
 * <p><b>현재 소비자가 0이다(의도된 상태).</b> 과거에는 {@code ProductReviewEventListener}가 이 이벤트로
 * {@code PRODUCT.rating}·{@code review_count}를 재집계했으나, 상품 평점의 근거가 MENU_REVIEW로 완전히
 * 이관되면서 그 구독이 {@code ProductMenuReviewEventListener}(MENU_REVIEW 이벤트 3종)로 옮겨갔다.
 *
 * <p>그럼에도 발행을 제거하지 않는 이유는 두 가지다 — (1) 발행측
 * ({@code ReviewLifecycleService#register})을 건드리면 이번 범위 밖의 회귀 위험이 생기고, (2) 알림 적재
 * 등 향후 소비처가 붙을 자리다. 미소비 이벤트를 정리한 과거 판단(커밋 1f3945b4)과 충돌하지 않도록
 * 여기에 부재 사실을 명시해 둔다.
 */
public record ReviewCreatedEvent(
    ReviewId reviewId,
    MemberId memberId,
    ShopId shopId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
