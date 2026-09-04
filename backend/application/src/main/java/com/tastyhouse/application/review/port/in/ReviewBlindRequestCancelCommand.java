package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 게시중단 요청 취소 command. 요청 본문이 없는 상태전이라 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ReviewBlindRequestCancelCommand(
    Long ceoId,
    Long shopId,
    Long blindRequestId
) {
    public ReviewBlindRequestCancelCommand {
        if (ceoId == null || shopId == null || blindRequestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRequestCancelCommand of(Long ceoId, Long shopId, Long blindRequestId) {
        return new ReviewBlindRequestCancelCommand(ceoId, shopId, blindRequestId);
    }
}
