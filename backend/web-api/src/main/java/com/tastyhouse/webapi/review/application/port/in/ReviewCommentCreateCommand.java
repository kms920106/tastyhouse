package com.tastyhouse.webapi.review.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 댓글 등록 command. 경로 변수 {@code reviewId}와 본문의 내용을 함께 담는다.
 */
public record ReviewCommentCreateCommand(
    Long memberId,
    Long reviewId,
    String content
) {
    public ReviewCommentCreateCommand {
        if (memberId == null || reviewId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
