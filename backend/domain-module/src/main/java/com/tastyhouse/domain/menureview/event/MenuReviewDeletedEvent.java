package com.tastyhouse.domain.menureview.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 평가 삭제 도메인 이벤트 — 상품 평점 재집계 트리거.
 *
 * <p>삭제되면 그 {@code order_product_id}에 다시 평가를 남길 수 있다(유니크 해제).
 */
public record MenuReviewDeletedEvent(
    MenuReviewId menuReviewId,
    MemberId memberId,
    ShopId shopId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
