package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 좋아요 토글 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ReviewLikeToggleCommand(
    Long memberId,
    Long reviewId
) {
    public ReviewLikeToggleCommand {
        if (memberId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewLikeToggleCommand of(Long memberId, Long reviewId) {
        return new ReviewLikeToggleCommand(memberId, reviewId);
    }
}
