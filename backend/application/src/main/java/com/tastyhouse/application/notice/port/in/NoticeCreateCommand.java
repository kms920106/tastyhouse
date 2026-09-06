package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record NoticeCreateCommand(
    String title,
    String content,
    boolean visible
) {
    public NoticeCreateCommand {
        if (title == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
