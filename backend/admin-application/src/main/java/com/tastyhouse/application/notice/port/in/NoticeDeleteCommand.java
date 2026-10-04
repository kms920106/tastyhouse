package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record NoticeDeleteCommand(Long noticeId) {

    public NoticeDeleteCommand {
        if (noticeId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static NoticeDeleteCommand of(Long noticeId) {
        return new NoticeDeleteCommand(noticeId);
    }
}
