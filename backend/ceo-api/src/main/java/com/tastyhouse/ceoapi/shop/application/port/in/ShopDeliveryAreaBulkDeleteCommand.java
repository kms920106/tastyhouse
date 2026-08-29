package com.tastyhouse.ceoapi.shop.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 배달가능지역 행정동 일괄 삭제 command.
 */
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
