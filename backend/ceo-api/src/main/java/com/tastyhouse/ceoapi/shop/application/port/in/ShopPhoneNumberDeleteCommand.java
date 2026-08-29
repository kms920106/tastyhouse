package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 전화번호 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopPhoneNumberDeleteCommand(
    Long ceoId,
    Long phoneNumberId
) {
    public ShopPhoneNumberDeleteCommand {
        if (ceoId == null || phoneNumberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhoneNumberDeleteCommand of(Long ceoId, Long phoneNumberId) {
        return new ShopPhoneNumberDeleteCommand(ceoId, phoneNumberId);
    }
}
