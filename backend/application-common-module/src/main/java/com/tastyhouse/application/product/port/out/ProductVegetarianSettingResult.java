package com.tastyhouse.application.product.port.out;

import com.tastyhouse.domain.product.model.VegetarianType;

/**
 * 메뉴에 현재 반영된 채식 설정 투영.
 *
 * <p>{@code vegetarianType}이 {@code null}이면 채식 메뉴가 아니다. 진실원은 {@code PRODUCT} 한 곳이라
 * 요청 이력이 여러 건 쌓여도 이 투영만 보면 현재 상태가 확정된다.
 */
public record ProductVegetarianSettingResult(
    Long productId,
    Long shopId,
    VegetarianType vegetarianType
) {
}
