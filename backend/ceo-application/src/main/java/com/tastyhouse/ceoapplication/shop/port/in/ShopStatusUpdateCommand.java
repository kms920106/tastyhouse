package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 노출 상태 변경 command.
 */
public record ShopStatusUpdateCommand(
    Long ceoId,
    Long shopId,
    String status
) {
    public ShopStatusUpdateCommand {
        if (ceoId == null || shopId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
