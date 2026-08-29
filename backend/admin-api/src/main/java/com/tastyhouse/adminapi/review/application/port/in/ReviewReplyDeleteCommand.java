package com.tastyhouse.adminapi.review.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 리뷰 답글 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ReviewReplyDeleteCommand(Long replyId) {
    public ReviewReplyDeleteCommand {
        if (replyId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewReplyDeleteCommand of(Long replyId) {
        return new ReviewReplyDeleteCommand(replyId);
    }
}
