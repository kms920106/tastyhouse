package com.tastyhouse.domain.menureview.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 평가 등록 도메인 이벤트 — 상품 평점 재집계 트리거.
 *
 * <p>수신자는 infrastructure-module의 {@code ProductMenuReviewEventListener} 하나뿐이다.
 * {@code PRODUCT.rating}의 유일한 근거가 MENU_REVIEW이므로 구독도 하나만 둔다.
 */
public record MenuReviewCreatedEvent(
    MenuReviewId menuReviewId,
    MemberId memberId,
    ShopId shopId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
