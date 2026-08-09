package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;

/**
 * 배달지역 조정 신청 한 건(점주 이력 목록·admin 검수 목록용).
 *
 * <p>{@code consentFileUrl}은 DAO가 {@code UPLOADED_FILE}을 조인해 {@code FileUrlResolver}로 완성한
 * 표시용 URL이다 — 응답에 {@code ~FileId}를 노출하지 않는다. {@code shopName}은 admin 목록에서만
 * 쓰이므로 점주 경로에서는 {@code null}이다.
 */
public record ShopDeliveryAreaAdjustmentListItemResult(
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
    LocalDateTime createdAt
) {
}
