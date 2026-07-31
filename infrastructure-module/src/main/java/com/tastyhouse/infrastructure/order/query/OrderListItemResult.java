package com.tastyhouse.infrastructure.order.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.payment.domain.model.PaymentStatus;

/**
 * 내 주문 목록 항목 조회 결과(web-api용) — 가게 대표 이미지·첫 상품명·상품 종류 수와 결제 상태를 함께 투영한다.
 *
 * <p>admin 관리 목록({@link OrderManagementListItemResult})과 필드 셋이 달라 통합하지 않는다 — 관리
 * 목록은 주문번호·주문자명·주문방식 등 운영 정보를 담고, 이 목록은 회원 화면에 필요한 가게·상품 정보를 담는다.
 */
public record OrderListItemResult(
    Long id,
    String shopName,
    String shopThumbnailImageFilePath,
    String firstProductName,
    Integer totalItemCount,
    Integer amount,
    PaymentStatus paymentStatus,
    LocalDateTime paymentDate
) {
    @QueryProjection
    public OrderListItemResult {
    }
}
