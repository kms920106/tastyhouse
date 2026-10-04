package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductFeedbackReadCommand(
    Long ceoId,
    Long shopId
) {

    public ProductFeedbackReadCommand {
        if (ceoId == null
            || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
