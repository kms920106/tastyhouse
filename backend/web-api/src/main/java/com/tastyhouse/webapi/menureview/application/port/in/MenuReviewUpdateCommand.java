package com.tastyhouse.webapi.menureview.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 평가 수정 command. 작성 근거인 주문 항목은 바꿀 수 없으므로 평점·코멘트만 담는다.
 */
public record MenuReviewUpdateCommand(
    Long memberId,
    Long menuReviewId,
    Integer rating,
    String comment
) {
    public MenuReviewUpdateCommand {
        if (memberId == null || menuReviewId == null || rating == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
