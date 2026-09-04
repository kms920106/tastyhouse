package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 공지 재게시 command. */
public record ShopNoticeUnhideCommand(
    Long adminId,
    Long noticeId
) {
    public ShopNoticeUnhideCommand {
        if (adminId == null || noticeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopNoticeUnhideCommand of(Long adminId, Long noticeId) {
        return new ShopNoticeUnhideCommand(adminId, noticeId);
    }
}
