package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
