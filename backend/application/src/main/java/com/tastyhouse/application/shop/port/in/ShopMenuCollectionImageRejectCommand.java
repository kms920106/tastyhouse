package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 메뉴판 이미지 반려 command. */
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
