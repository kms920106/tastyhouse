package com.tastyhouse.adminapi.product.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 매장 가격 인증 요청 반려 command. 반려 사유는 필수다 — 점주가 무엇을 고쳐 다시 요청해야 하는지 알아야 한다. */
public record StorePriceVerificationRejectCommand(Long verificationId, String rejectReason) {
    public StorePriceVerificationRejectCommand {
        if (verificationId == null || rejectReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
