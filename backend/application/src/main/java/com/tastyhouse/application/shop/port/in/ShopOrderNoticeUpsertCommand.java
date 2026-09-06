package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopOrderNoticeUpsertCommand(
    Long ceoId,
    Long shopId,
    String content
) {
    public ShopOrderNoticeUpsertCommand {
        if (ceoId == null || shopId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
