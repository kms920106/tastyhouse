package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 매장 가격 인증 요청 승인 command. 요청 본문이 없는 상태 전이라 컨트롤러가 정적 팩토리로 조립한다. */
public record StorePriceVerificationApproveCommand(Long verificationId) {
    public StorePriceVerificationApproveCommand {
        if (verificationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static StorePriceVerificationApproveCommand of(Long verificationId) {
        return new StorePriceVerificationApproveCommand(verificationId);
    }
}
