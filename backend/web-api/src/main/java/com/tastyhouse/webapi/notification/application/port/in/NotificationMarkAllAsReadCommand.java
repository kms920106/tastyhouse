package com.tastyhouse.webapi.notification.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 알림 전체 읽음 처리 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record NotificationMarkAllAsReadCommand(Long memberId) {
    public NotificationMarkAllAsReadCommand {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static NotificationMarkAllAsReadCommand of(Long memberId) {
        return new NotificationMarkAllAsReadCommand(memberId);
    }
}
