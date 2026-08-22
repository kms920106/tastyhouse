package com.tastyhouse.infrastructure.product.query;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 사장님 추천(대표 메뉴) 지정 요청 투영.
 *
 * <p>{@code imageUrl}을 목록 항목에 담는 이유는 그것이 <b>검수의 실질적 근거</b>이기 때문이다 —
 * 대표 메뉴는 가게 상단에 사진으로 노출되므로, 검수자가 상세를 다시 열지 않고 사진을 보고 판정할 수
 * 있어야 한다(채식 요청이 재료를 목록에 함께 담는 것과 같은 이유).
 *
 * <p>{@code shopId}는 요청 행이 직접 들고 있는 값을 쓴다 — 메뉴를 거쳐 역조회하지 않는다.
 */
public record ProductRepresentativeRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String shopName,
    String productName,
    String imageUrl,
    ApprovalStatus status,
    String rejectReason
) {

}
