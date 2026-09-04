package com.tastyhouse.application.order.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 상품 한 줄 command.
 *
 * <p>과거 서비스가 {@code OrderProductRequest}(HTTP 요청 record)를 그대로 받던 자리를 대체한다(챕터 02 §5).
 *
 * <p>{@code priceId}·{@code options}는 선택값이라 null을 허용한다 — 기본 가격 적용·옵션 미선택이
 * 정상 흐름이며, 그 해석은 도메인 서비스가 담당한다.
 */
public record OrderLineCommand(
    Long productId,
    Long priceId,
    List<OrderLineOptionCommand> options,
    Integer quantity
) {
    public OrderLineCommand {
        if (productId == null || quantity == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static OrderLineCommand of(
        Long productId,
        Long priceId,
        List<OrderLineOptionCommand> options,
        Integer quantity
    ) {
        return new OrderLineCommand(
            productId,
            priceId,
            options,
            quantity
        );
    }
}
