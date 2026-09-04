package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 알림 단건 읽음 처리 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 *
 * <p>대상 회원은 경로/바디가 아니라 토큰에서만 온다 — 요청으로 받으면 그 자체가 IDOR 입구가 된다.
 */
public record NotificationMarkAsReadCommand(
    Long notificationId,
    Long memberId
) {
    public NotificationMarkAsReadCommand {
        if (notificationId == null || memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static NotificationMarkAsReadCommand of(Long notificationId, Long memberId) {
        return new NotificationMarkAsReadCommand(notificationId, memberId);
    }
}
