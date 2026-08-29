package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 공지 게시중단 command. {@code adminId}는 조치 이력의 액터라 principal에서 주입한다. */
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
