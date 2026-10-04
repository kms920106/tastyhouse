package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewBlindRequestCreateCommand(
    Long ceoId,
    Long shopId,
    Long reviewId,
    String reason,
    String detailReason,
    List<Long> attachmentFileIds
) {

    public ReviewBlindRequestCreateCommand {
        if (ceoId == null || shopId == null || reviewId == null || reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
