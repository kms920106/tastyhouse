package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewBlindRequestCancelCommand(
    Long ceoId,
    Long shopId,
    Long blindRequestId
) {

    public ReviewBlindRequestCancelCommand {
        if (ceoId == null || shopId == null || blindRequestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRequestCancelCommand of(Long ceoId, Long shopId, Long blindRequestId) {
        return new ReviewBlindRequestCancelCommand(ceoId, shopId, blindRequestId);
    }
}
