package com.tastyhouse.domain.notification.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 알림을 눌렀을 때 이동할 대상의 유형.
 *
 * <p>{@code targetType}·{@code targetId}는 함께 null일 수 있다 — 이동 대상이 없는 공지성 알림을
 * 표현하기 위함이다. 알림함이 유형별 상세 화면 경로를 알아야 하므로 대상 식별자만으로는 부족하다.
 */
public enum NotificationTargetType {

    /** 리뷰 상세로 이동한다({@code targetId} = reviewId). */
    REVIEW;

    public static NotificationTargetType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.NOTIFICATION_TYPE_UNKNOWN,
                ErrorCode.NOTIFICATION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
