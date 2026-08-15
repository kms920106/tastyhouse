package com.tastyhouse.domain.product.service;

/**
 * 주문 라인에서 선택한 옵션 한 건의 <b>요청</b> — 상품 검증 서비스의 입력이다.
 *
 * <p>{@code groupId}와 {@code optionId}가 둘 다 {@code Long}이라 순서를 바꿔도 컴파일되므로,
 * 조립·검증 시 두 값의 자리를 반드시 대조한다.
 */
public record OrderLineOptionSelection(
    Long groupId,
    Long optionId
) {

    public static OrderLineOptionSelection of(Long groupId, Long optionId) {
        return new OrderLineOptionSelection(groupId, optionId);
    }
}
