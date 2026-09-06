package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopNoticeCreateCommand(
    Long ceoId,
    Long shopId,
    String content,
    Boolean exposed
) {
    public ShopNoticeCreateCommand {
        if (ceoId == null || shopId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
