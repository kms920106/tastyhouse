package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductFeedbackCreateCommand(
    Long memberId,
    Long productId,
    String feedbackType,
    String content
) {

    public ProductFeedbackCreateCommand {
        if (memberId == null || productId == null || feedbackType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
