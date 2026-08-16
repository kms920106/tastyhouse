package com.tastyhouse.domain.notification.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 인앱 알림 유형.
 *
 * <p>지금은 사장님 답변 알림 하나뿐이지만 enum으로 두는 이유는, 알림함이 유형별로 다른 아이콘·이동 경로를
 * 갖는 화면이라 소비처가 문자열이 아니라 닫힌 후보 집합으로 분기해야 하기 때문이다.
 */
public enum NotificationType {

    /** 내가 쓴 리뷰에 사장님이 답변을 달았다. */
    REVIEW_OWNER_REPLY;

    public static NotificationType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.NOTIFICATION_TYPE_UNKNOWN,
                ErrorCode.NOTIFICATION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
