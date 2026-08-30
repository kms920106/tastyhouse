package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션 숨김 처리 command.
 */
public record ProductOptionHideCommand(
    Long ceoId,
    Long shopId,
    List<ProductOptionTargetCommand> options
) {
    public ProductOptionHideCommand {
        if (ceoId == null
            || shopId == null
            || options == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
