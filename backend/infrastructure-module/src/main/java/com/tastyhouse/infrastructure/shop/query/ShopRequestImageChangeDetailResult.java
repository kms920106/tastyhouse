package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageType;

/**
 * 이미지 변경요청 원본 투영(요청처리 현황 상세의 유형별 부분).
 *
 * <p>반드시 {@code public}이어야 한다 — 이유는 {@code ShopRequestListItemResult} Javadoc 참조.
 *
 * <p>{@code status}·{@code rejectReason}을 함께 투영하는 이유는 <b>상세 응답의 진실원이 원본</b>이기
 * 때문이다 — 인덱스 값이 아니라 이 값으로 응답한다.
 */
public record ShopRequestImageChangeDetailResult(
    ShopImageType imageType,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {
}
