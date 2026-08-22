package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴모음컷 투영 — 점주 화면용.
 *
 * <p>{@code status}·{@code rejectReason}을 함께 담는 이유는 원문 규격이 점주 화면에 대기/승인/취소
 * 상태를 보여주도록 규정하기 때문이다. 손님 화면(web-api)은 승인분만 보므로 이 두 필드가 없는
 * {@link ShopMenuCollectionImageExposureResult}를 따로 쓴다.
 */
public record ShopMenuCollectionImageResult(
    Long id,
    String imageUrl,
    Integer sort,
    ApprovalStatus status,
    String rejectReason
) {
}
