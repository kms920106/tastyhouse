package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductFeedbackReadCommand(
    Long ceoId,
    Long shopId
) {
    public ProductFeedbackReadCommand {
        if (ceoId == null
            || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
