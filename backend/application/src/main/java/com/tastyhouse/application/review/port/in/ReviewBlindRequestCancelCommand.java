package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewBlindRequestCancelCommand(
    Long ceoId,
    Long shopId,
    Long blindRequestId
) {
    public ReviewBlindRequestCancelCommand {
        if (ceoId == null || shopId == null || blindRequestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRequestCancelCommand of(Long ceoId, Long shopId, Long blindRequestId) {
        return new ReviewBlindRequestCancelCommand(ceoId, shopId, blindRequestId);
    }
}
