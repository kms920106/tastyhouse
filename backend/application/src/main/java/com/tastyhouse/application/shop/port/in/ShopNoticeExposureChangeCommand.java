package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 공지 앱 노출 토글 command.
 */
public record ShopNoticeExposureChangeCommand(
    Long ceoId,
    Long shopId,
    Long noticeId,
    Boolean exposed
) {
    public ShopNoticeExposureChangeCommand {
        if (ceoId == null || shopId == null || noticeId == null || exposed == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
