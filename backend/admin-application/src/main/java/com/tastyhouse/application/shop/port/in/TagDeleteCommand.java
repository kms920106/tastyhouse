package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record TagDeleteCommand(
    Long tagId
) {

    public TagDeleteCommand {
        if (tagId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static TagDeleteCommand of(Long tagId) {
        return new TagDeleteCommand(tagId);
    }
}
