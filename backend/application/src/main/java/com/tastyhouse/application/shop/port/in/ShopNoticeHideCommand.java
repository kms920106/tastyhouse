package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopNoticeHideCommand(
    Long adminId,
    Long noticeId,
    String reason
) {
    public ShopNoticeHideCommand {
        if (adminId == null || noticeId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
