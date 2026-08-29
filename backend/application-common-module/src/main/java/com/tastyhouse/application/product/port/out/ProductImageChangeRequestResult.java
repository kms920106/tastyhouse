package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 이미지 변경 승인요청 투영.
 *
 * <p>{@code shopId}는 {@code PRODUCT_IMAGE_CHANGE_REQUEST}에 없어 {@code PRODUCT}를 조인해 담는다 —
 * 관리자 검수 목록이 어느 가게 요청인지 보여야 하고, 점주 경로에서는 소유 가게 재확인 근거가 된다.
 */
public record ProductImageChangeRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String productName,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {
}
