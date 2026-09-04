package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 배달가능지역(행정동) 등록 command.
 */
public record ShopDeliveryAreaCreateCommand(
    Long ceoId,
    Long shopId,
    Long adminDongId
) {
    public ShopDeliveryAreaCreateCommand {
        if (ceoId == null || shopId == null || adminDongId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
