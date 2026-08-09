package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

/**
 * 배달지역 조정 신청 상세(admin 검수 화면용).
 *
 * <p>목록 필드 전체에 중첩 사유·동의서 URL·반려 사유·수정 일시를 더한 형태다. {@code consentFileUrl}은
 * DAO가 {@code UPLOADED_FILE}을 조인해 완성한 표시용 URL이다.
 */
public record ShopDeliveryAreaAdjustmentDetailResult(
    Long id,
    Long shopId,
    String shopName,
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason,
    String consentFileUrl,
    DeliveryAreaAdjustmentStatus status,
    String rejectReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
