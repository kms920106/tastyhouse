package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopMenuCollectionImageReorderCommand(
    Long ceoId,
    Long shopId,
    List<Long> imageIds
) {
    public ShopMenuCollectionImageReorderCommand {
        if (ceoId == null || shopId == null || imageIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
