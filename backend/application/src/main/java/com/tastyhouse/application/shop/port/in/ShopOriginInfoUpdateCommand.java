package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopOriginInfoUpdateCommand(
    Long ceoId,
    Long shopId,
    String sourceType,
    String content,
    String url
) {
    public ShopOriginInfoUpdateCommand {
        if (ceoId == null || shopId == null || sourceType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
