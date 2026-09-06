package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductFeedbackCreateCommand(
    Long memberId,
    Long productId,
    String feedbackType,
    String content
) {
    public ProductFeedbackCreateCommand {
        if (memberId == null || productId == null || feedbackType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
