package com.tastyhouse.domain.menureview.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 평가 평점 변경 도메인 이벤트 — 상품 평점 재집계 트리거.
 *
 * <p>평점 수정·숨김 전이처럼 <b>건수는 그대로인데 집계 결과가 달라지는</b> 전이에서 발행한다. 등록·삭제와
 * 이벤트를 나눈 이유는 수신 측이 같은 재집계를 하더라도 "무엇이 일어났는가"가 로그·향후 소비처에서
 * 구분되어야 하기 때문이다.
 */
public record MenuReviewRatingChangedEvent(
    MenuReviewId menuReviewId,
    MemberId memberId,
    ShopId shopId,
    ProductId productId,
    LocalDateTime occurredAt
) {
}
