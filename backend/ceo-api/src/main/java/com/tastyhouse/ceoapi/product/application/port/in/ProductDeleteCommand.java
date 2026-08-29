package com.tastyhouse.ceoapi.product.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 삭제 command.
 */
public record ProductDeleteCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds
) {
    public ProductDeleteCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
