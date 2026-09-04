package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 숨김 처리 command.
 */
public record ProductHideCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds
) {
    public ProductHideCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
