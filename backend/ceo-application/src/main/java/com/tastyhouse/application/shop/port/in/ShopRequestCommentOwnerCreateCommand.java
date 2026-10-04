package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopRequestCommentOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    Long requestId,
    String content
) {

    public ShopRequestCommentOwnerCreateCommand {
        if (ceoId == null || shopId == null || requestId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
