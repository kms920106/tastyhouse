package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record NoticeUpdateCommand(
    Long noticeId,
    String title,
    String content,
    boolean visible
) {
    public NoticeUpdateCommand {
        if (noticeId == null || title == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
