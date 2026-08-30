package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 옵션 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand}로 주입한다.
 */
public record ProductOptionUpdateCommand(
    Long ceoId,
    Long optionId,
    Long shopId,
    String name,
    Integer additionalPrice,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
    public ProductOptionUpdateCommand {
        if (ceoId == null
            || optionId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
