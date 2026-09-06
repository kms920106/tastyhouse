package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryAreaBulkDeleteCommand(
    Long ceoId,
    Long shopId,
    List<Long> adminDongIds
) {
    public ShopDeliveryAreaBulkDeleteCommand {
        if (ceoId == null || shopId == null || adminDongIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
