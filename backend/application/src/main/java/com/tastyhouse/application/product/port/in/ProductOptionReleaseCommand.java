package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionReleaseCommand(
    Long ceoId,
    Long shopId,
    List<ProductOptionTargetCommand> options,
    String target
) {
    public ProductOptionReleaseCommand {
        if (ceoId == null
            || shopId == null
            || options == null
            || target == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
