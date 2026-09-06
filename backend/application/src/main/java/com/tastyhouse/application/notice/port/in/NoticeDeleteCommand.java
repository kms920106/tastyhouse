package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record NoticeDeleteCommand(Long noticeId) {
    public NoticeDeleteCommand {
        if (noticeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static NoticeDeleteCommand of(Long noticeId) {
        return new NoticeDeleteCommand(noticeId);
    }
}
