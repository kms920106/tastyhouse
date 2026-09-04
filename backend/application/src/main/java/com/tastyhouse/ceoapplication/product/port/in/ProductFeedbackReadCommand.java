package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 고객 의견 읽음 처리 command.
 */
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
