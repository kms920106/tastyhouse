package com.tastyhouse.adminapplication.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 리뷰 게시중단 요청 반려 command. 사유는 필수다. */
public record ReviewBlindRequestRejectCommand(Long requestId, String rejectReason) {
    public ReviewBlindRequestRejectCommand {
        if (requestId == null || rejectReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
