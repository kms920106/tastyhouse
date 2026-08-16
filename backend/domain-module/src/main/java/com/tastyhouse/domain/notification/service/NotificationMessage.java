package com.tastyhouse.domain.notification.service;

/**
 * 알림 문구 소유자(package-private 유틸).
 *
 * <p>문구를 소비처(리스너 등)마다 직접 조립하게 두면 같은 알림인데 경로에 따라 표현이 갈린다. 유형별 문구를
 * 이 한 곳에 모아 두면 소비처가 늘어도 표현이 하나로 유지되고, 문구 변경이 한 파일 수정으로 끝난다.
 *
 * <p>같은 패키지의 {@link NotificationService}에서만 쓰므로 노출을 좁힌다(도메인 서비스 전용 문구 유틸의
 * 기존 선례를 따른다).
 */
final class NotificationMessage {

    private static final String REVIEW_OWNER_REPLY_TITLE = "사장님 답변이 등록되었어요";
    private static final String REVIEW_OWNER_REPLY_BODY_FORMAT = "%s 사장님이 회원님의 리뷰에 답변을 남겼어요.";

    private NotificationMessage() {
    }

    static String reviewOwnerReplyTitle() {
        return REVIEW_OWNER_REPLY_TITLE;
    }

    /**
     * @param shopName 가게명. 조회에 실패해 비어 있을 수 있으므로 그 경우 가게명 없는 문구로 대체한다 —
     *                 알림 본문에 "null 사장님"이 노출되는 것을 막기 위함이다
     */
    static String reviewOwnerReplyBody(String shopName) {
        if (shopName == null || shopName.isBlank()) {
            return "회원님의 리뷰에 사장님 답변이 등록되었어요.";
        }
        return String.format(REVIEW_OWNER_REPLY_BODY_FORMAT, shopName);
    }
}
