package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 소개(사장님 한마디) 등록·수정 command.
 */
public record ShopIntroductionUpdateCommand(
    Long ceoId,
    Long shopId,
    String message
) {
    public ShopIntroductionUpdateCommand {
        if (ceoId == null || shopId == null || message == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
