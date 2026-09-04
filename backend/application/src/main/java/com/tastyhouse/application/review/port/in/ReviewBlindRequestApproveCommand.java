package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 리뷰 게시중단 요청 승인 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ReviewBlindRequestApproveCommand(Long requestId) {
    public ReviewBlindRequestApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRequestApproveCommand of(Long requestId) {
        return new ReviewBlindRequestApproveCommand(requestId);
    }
}
