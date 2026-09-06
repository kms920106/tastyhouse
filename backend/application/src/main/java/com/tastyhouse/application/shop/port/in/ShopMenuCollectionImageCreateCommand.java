package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopMenuCollectionImageCreateCommand(
    Long ceoId,
    Long shopId
) {
    public ShopMenuCollectionImageCreateCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageCreateCommand of(Long ceoId, Long shopId) {
        return new ShopMenuCollectionImageCreateCommand(ceoId, shopId);
    }
}
