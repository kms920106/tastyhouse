package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 채식 설정 승인요청 투영.
 *
 * <p>{@code ingredients}·{@code description}은 관리자 검수의 유일한 근거라 목록 항목에도 담는다 —
 * 검수자가 상세를 다시 열지 않고 판정할 수 있어야 한다.
 */
public record ProductVegetarianRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String productName,
    VegetarianType vegetarianType,
    String ingredients,
    String description,
    ApprovalStatus status,
    String rejectReason
) {
}
