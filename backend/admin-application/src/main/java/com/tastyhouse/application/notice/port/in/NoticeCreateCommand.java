package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record NoticeCreateCommand(
    String title,
    String content,
    boolean visible
) {

    public NoticeCreateCommand {
        if (title == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
