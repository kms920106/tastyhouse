package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopMenuCollectionImageReorderCommand(
    Long ceoId,
    Long shopId,
    List<Long> imageIds
) {

    public ShopMenuCollectionImageReorderCommand {
        if (ceoId == null || shopId == null || imageIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
