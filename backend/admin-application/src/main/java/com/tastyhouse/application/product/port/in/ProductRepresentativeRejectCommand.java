package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductRepresentativeRejectCommand(Long requestId, String rejectReason) {

    public ProductRepresentativeRejectCommand {
        if (requestId == null || rejectReason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
