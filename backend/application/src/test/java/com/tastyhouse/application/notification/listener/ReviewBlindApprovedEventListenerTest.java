package com.tastyhouse.application.notification.listener;

import java.time.LocalDateTime;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.notification.service.NotificationService;
import com.tastyhouse.application.shared.listener.ListenerLogCapture;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.event.ReviewBlindApprovedEvent;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewBlindApprovedEventListenerTest {
    private static final ReviewId REVIEW_ID = ReviewId.of(482L);
    private static final MemberId REVIEWER_MEMBER_ID = MemberId.of(42L);
    private static final ReviewBlindRequestId BLIND_REQUEST_ID = ReviewBlindRequestId.of(93L);
    private static final LocalDateTime BLIND_UNTIL = LocalDateTime.of(2026, 7, 20, 0, 0);

    private final NotificationService notificationService = mock(NotificationService.class);
    private final ReviewBlindApprovedEventListener listener =
        new ReviewBlindApprovedEventListener(notificationService);

    private ListenerLogCapture logCapture;

    @AfterEach
    void tearDown() {
        if (logCapture != null) {
            logCapture.detach();
        }
    }

    @Test
    @DisplayName("게시중단 승인 이벤트를 받으면 리뷰 작성자 앞으로 게시중단 기한을 담은 알림을 적재한다")
    void storesNotificationForReviewer() {
        listener.handle(event());

        verify(notificationService).notifyReviewBlindApproved(REVIEWER_MEMBER_ID, REVIEW_ID, BLIND_UNTIL);
    }

    @Test
    @DisplayName("적재 결과를 식별자와 함께 로그로 남긴다")
    void logsStoredNotification() {
        logCapture = ListenerLogCapture.attachTo(ReviewBlindApprovedEventListener.class);
        when(notificationService.notifyReviewBlindApproved(REVIEWER_MEMBER_ID, REVIEW_ID, BLIND_UNTIL))
            .thenReturn(501L);

        listener.handle(event());

        assertThat(logCapture.events())
            .singleElement()
            .extracting(ILoggingEvent::getFormattedMessage)
            .asString()
            .contains("notificationId=501")
            .contains("reviewId=" + REVIEW_ID.value())
            .contains("memberId=" + REVIEWER_MEMBER_ID.value())
            .contains("blindRequestId=" + BLIND_REQUEST_ID.value());
    }

    private static ReviewBlindApprovedEvent event() {
        return ReviewBlindApprovedEvent.of(
            REVIEW_ID,
            REVIEWER_MEMBER_ID,
            BLIND_REQUEST_ID,
            BLIND_UNTIL,
            LocalDateTime.of(2026, 6, 20, 14, 3)
        );
    }
}
