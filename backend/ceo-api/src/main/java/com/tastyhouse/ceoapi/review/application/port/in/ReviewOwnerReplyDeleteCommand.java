package com.tastyhouse.ceoapi.review.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 사장님 답변 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ReviewOwnerReplyDeleteCommand(
    Long ceoId,
    Long shopId,
    Long reviewId
) {
    public ReviewOwnerReplyDeleteCommand {
        if (ceoId == null || shopId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewOwnerReplyDeleteCommand of(Long ceoId, Long shopId, Long reviewId) {
        return new ReviewOwnerReplyDeleteCommand(ceoId, shopId, reviewId);
    }
}
