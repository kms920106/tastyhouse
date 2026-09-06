package com.tastyhouse.domain.notification.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;

final class NotificationMessage {
    private static final String REVIEW_OWNER_REPLY_TITLE = "사장님 답변이 등록되었어요";
    private static final String REVIEW_OWNER_REPLY_BODY_FORMAT = "%s 사장님이 회원님의 리뷰에 답변을 남겼어요.";

    private static final DateTimeFormatter BLIND_UNTIL_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    private static final String REVIEW_BLIND_APPROVED_TITLE = "작성하신 리뷰가 게시중단되었어요";
    private static final String REVIEW_BLIND_APPROVED_BODY_FORMAT =
        "회원님의 리뷰가 %d일간 게시중단되었어요. 삭제에 동의하시면 지금 삭제되고, 동의하지 않으시면 %s 이후 다시 노출돼요.";

    private NotificationMessage() {
    }

    static String reviewOwnerReplyTitle() {
        return REVIEW_OWNER_REPLY_TITLE;
    }

    static String reviewOwnerReplyBody(String shopName) {
        if (shopName == null || shopName.isBlank()) {
            return "회원님의 리뷰에 사장님 답변이 등록되었어요.";
        }
        return String.format(REVIEW_OWNER_REPLY_BODY_FORMAT, shopName);
    }

    static String reviewBlindApprovedTitle() {
        return REVIEW_BLIND_APPROVED_TITLE;
    }

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
