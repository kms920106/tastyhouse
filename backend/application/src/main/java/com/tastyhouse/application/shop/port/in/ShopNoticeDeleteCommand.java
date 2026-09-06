package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopNoticeDeleteCommand(
    Long ceoId,
    Long shopId,
    Long noticeId
) {
    public ShopNoticeDeleteCommand {
        if (ceoId == null || shopId == null || noticeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopNoticeDeleteCommand of(Long ceoId, Long shopId, Long noticeId) {
        return new ShopNoticeDeleteCommand(ceoId, shopId, noticeId);
    }
}
