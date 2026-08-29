package com.tastyhouse.adminapi.product.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 매장 가격 인증 요청 검수 착수 command. 요청 본문이 없는 상태 전이라 컨트롤러가 정적 팩토리로 조립한다. */
public record StorePriceVerificationStartReviewCommand(Long verificationId) {
    public StorePriceVerificationStartReviewCommand {
        if (verificationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static StorePriceVerificationStartReviewCommand of(Long verificationId) {
        return new StorePriceVerificationStartReviewCommand(verificationId);
    }
}
