package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

/**
 * 배달지역 조정 신청 원본 투영(요청처리 현황 상세의 유형별 부분).
 *
 * <p>반드시 {@code public}이어야 한다 — 이유는 {@code ShopRequestListItemResult} Javadoc 참조.
 *
 * <p>{@code status}·{@code rejectReason}을 함께 투영하는 이유는 상세 응답의 진실원이 원본이기 때문이다.
 * 기존 {@code ShopDeliveryAreaAdjustmentDetailResult}를 재사용하지 않는 이유는 그쪽이 admin 검수 화면용으로
 * 가게명·감사 시각을 함께 담고 있어, 점주 통합 상세가 쓰지 않는 필드와 join이 따라붙기 때문이다.
 */
public record ShopRequestAdjustmentDetailResult(
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason,
    String consentFileUrl,
    DeliveryAreaAdjustmentStatus status,
    String rejectReason
) {
}
