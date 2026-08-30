package com.tastyhouse.webapplication.menureview.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 평가 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record MenuReviewDeleteCommand(
    Long memberId,
    Long menuReviewId
) {
    public MenuReviewDeleteCommand {
        if (memberId == null || menuReviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static MenuReviewDeleteCommand of(Long memberId, Long menuReviewId) {
        return new MenuReviewDeleteCommand(memberId, menuReviewId);
    }
}
