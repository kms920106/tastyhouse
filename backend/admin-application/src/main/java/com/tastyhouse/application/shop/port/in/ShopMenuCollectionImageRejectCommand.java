package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopMenuCollectionImageRejectCommand(
    Long imageId,
    String rejectReason
) {

    public ShopMenuCollectionImageRejectCommand {
        if (imageId == null || rejectReason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
