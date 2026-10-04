package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductImageChangeApproveCommand(Long requestId) {

    public ProductImageChangeApproveCommand {
        if (requestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductImageChangeApproveCommand of(Long requestId) {
        return new ProductImageChangeApproveCommand(requestId);
    }
}
