package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 대표번호 지정 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopPhoneNumberPrimaryDesignateCommand(
    Long ceoId,
    Long phoneNumberId
) {
    public ShopPhoneNumberPrimaryDesignateCommand {
        if (ceoId == null || phoneNumberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhoneNumberPrimaryDesignateCommand of(Long ceoId, Long phoneNumberId) {
        return new ShopPhoneNumberPrimaryDesignateCommand(ceoId, phoneNumberId);
    }
}
