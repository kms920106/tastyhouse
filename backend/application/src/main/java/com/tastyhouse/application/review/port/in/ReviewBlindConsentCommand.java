package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 게시중단 리뷰 삭제 동의 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ReviewBlindConsentCommand(
    Long memberId,
    Long reviewId
) {
    public ReviewBlindConsentCommand {
        if (memberId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindConsentCommand of(Long memberId, Long reviewId) {
        return new ReviewBlindConsentCommand(memberId, reviewId);
    }
}
