package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopRequestCommentManagementCreateCommand(
    Long requestId,
    Long adminId,
    String content
) {

    public ShopRequestCommentManagementCreateCommand {
        if (requestId == null || adminId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
