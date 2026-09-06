package com.tastyhouse.domain.notification.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;

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

    /** 안내 문구용 날짜 표기(예: {@code 2026년 9월 16일}). 시각까지는 노출하지 않는다. */
    private static final DateTimeFormatter BLIND_UNTIL_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    private static final String REVIEW_BLIND_APPROVED_TITLE = "작성하신 리뷰가 게시중단되었어요";
    private static final String REVIEW_BLIND_APPROVED_BODY_FORMAT =
        "회원님의 리뷰가 %d일간 게시중단되었어요. 삭제에 동의하시면 지금 삭제되고, 동의하지 않으시면 %s 이후 다시 노출돼요.";

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

    static String reviewBlindApprovedTitle() {
        return REVIEW_BLIND_APPROVED_TITLE;
    }

    /**
     * 게시중단 안내 본문.
     *
     * <p>안내에 노출되는 게시중단 기간은 {@link ReviewBlindRequest#BLIND_PERIOD_DAYS}를 참조한다 —
     * 리터럴로 복제하면 한쪽만 바뀌어 "실제 30일 / 안내 문구 14일" 같은 불일치가 테스트에도 걸리지 않고
     * 생긴다(인증코드 유효시간 상수를 도메인 모델이 단독 소유하는 것과 같은 이유).
     *
     * @param blindUntil 재노출 예정일시. 비어 있으면 날짜 없는 문구로 대체한다 — 본문에 "null 이후"가
     *                   노출되는 것을 막기 위함이다
     */
    static String reviewBlindApprovedBody(LocalDateTime blindUntil) {
        if (blindUntil == null) {
            return String.format(
                "회원님의 리뷰가 %d일간 게시중단되었어요. 삭제에 동의하시면 지금 삭제되고, 동의하지 않으시면 기간 경과 후 다시 노출돼요.",
                ReviewBlindRequest.BLIND_PERIOD_DAYS
            );
        }
        return String.format(
            REVIEW_BLIND_APPROVED_BODY_FORMAT,
            ReviewBlindRequest.BLIND_PERIOD_DAYS,
            blindUntil.format(BLIND_UNTIL_FORMATTER)
        );
    }
}
