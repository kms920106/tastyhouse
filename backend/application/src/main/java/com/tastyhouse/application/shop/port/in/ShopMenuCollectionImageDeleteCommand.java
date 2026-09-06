package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopMenuCollectionImageDeleteCommand(
    Long ceoId,
    Long shopId,
    Long imageId
) {
    public ShopMenuCollectionImageDeleteCommand {
        if (ceoId == null || shopId == null || imageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageDeleteCommand of(Long ceoId, Long shopId, Long imageId) {
        return new ShopMenuCollectionImageDeleteCommand(ceoId, shopId, imageId);
    }
}
