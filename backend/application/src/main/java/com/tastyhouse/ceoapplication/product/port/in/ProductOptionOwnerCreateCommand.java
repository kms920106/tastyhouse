package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 옵션 등록 command.
 *
 * <p>같은 타입({@code Integer})의 금액·수량 필드가 연달아 있어 위치 기반 조립이 조용히
 * 뒤바뀔 수 있으므로, {@code toCommand}는 반드시 이름 기반 접근자로 조립한다.
 */
public record ProductOptionOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
    public ProductOptionOwnerCreateCommand {
        if (ceoId == null
            || shopId == null
            || optionGroupId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
