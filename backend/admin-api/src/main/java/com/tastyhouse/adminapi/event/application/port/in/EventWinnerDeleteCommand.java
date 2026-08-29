package com.tastyhouse.adminapi.event.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 당첨자 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record EventWinnerDeleteCommand(Long winnerId) {
    public EventWinnerDeleteCommand {
        if (winnerId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static EventWinnerDeleteCommand of(Long winnerId) {
        return new EventWinnerDeleteCommand(winnerId);
    }
}
