package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryAreaBulkCreateCommand(
    Long ceoId,
    Long shopId,
    List<Long> adminDongIds
) {

    public ShopDeliveryAreaBulkCreateCommand {
        if (ceoId == null || shopId == null || adminDongIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
