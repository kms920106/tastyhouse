package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopNoticeUpdateCommand(
    Long ceoId,
    Long shopId,
    Long noticeId,
    String content,
    Boolean keepExistingImages
) {
    public ShopNoticeUpdateCommand {
        if (ceoId == null || shopId == null || noticeId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
