package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductRepresentativeApproveCommand(Long requestId) {

    public ProductRepresentativeApproveCommand {
        if (requestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductRepresentativeApproveCommand of(Long requestId) {
        return new ProductRepresentativeApproveCommand(requestId);
    }
}
