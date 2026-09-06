package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopMenuCollectionImageRejectCommand(
    Long imageId,
    String rejectReason
) {
    public ShopMenuCollectionImageRejectCommand {
        if (imageId == null || rejectReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
