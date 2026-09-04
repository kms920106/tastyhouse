package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 가격 항목 command.
 *
 * <p>{@code priceId}는 기존 행 갱신 시에만 채워지고 신규 행이면 null이다.
 * 같은 타입({@code Integer})의 금액 필드가 연달아 있어 위치 기반 조립이 조용히 뒤바뀔 수 있으므로,
 * {@code toCommand}는 반드시 이름 기반 접근자로 조립한다.
 */
public record ProductPriceItemCommand(
    Long priceId,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort
) {
    public ProductPriceItemCommand {
        if (priceName == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
