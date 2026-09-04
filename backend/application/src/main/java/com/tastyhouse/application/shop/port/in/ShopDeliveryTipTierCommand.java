package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달팁 주문금액 구간 한 줄 command.
 *
 * <p>과거 서비스가 {@code ShopDeliveryTipTierItemRequest}(HTTP 요청 record)를 그대로 받던 자리를
 * 대체한다(챕터 02 §5).
 *
 * <p>필드 순서는 {@code ShopDeliveryTipTierSpec.of(minOrderAmount, tipAmount)}와 동일하다 —
 * 둘 다 {@code Integer}라 순서가 어긋나도 컴파일되고 금액만 조용히 뒤바뀐다.
 */
public record ShopDeliveryTipTierCommand(
    Integer minOrderAmount,
    Integer tipAmount
) {
    public ShopDeliveryTipTierCommand {
        if (minOrderAmount == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipTierCommand of(Integer minOrderAmount, Integer tipAmount) {
        return new ShopDeliveryTipTierCommand(minOrderAmount, tipAmount);
    }
}
