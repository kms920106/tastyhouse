package com.tastyhouse.infrastructure.notification.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.service.NotificationService;
import com.tastyhouse.domain.review.event.ReviewOwnerReplyCreatedEvent;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewOwnerReplyEventListenerTest {
    private static final ReviewId REVIEW_ID = ReviewId.of(482L);
    private static final MemberId REVIEWER_MEMBER_ID = MemberId.of(42L);
    private static final ShopId SHOP_ID = ShopId.of(7L);
    private static final ReviewOwnerReplyId OWNER_REPLY_ID = ReviewOwnerReplyId.of(77L);
    private static final String SHOP_NAME = "BBQ치킨 성내점";

    private final RecordingNotificationService notificationService = new RecordingNotificationService();
    private final StubShopQueryDao shopQueryDao = new StubShopQueryDao();
    private final ReviewOwnerReplyEventListener listener =
        new ReviewOwnerReplyEventListener(notificationService, shopQueryDao);

    private ListenerLogCapture logCapture;

    @AfterEach
    void tearDown() {
        if (logCapture != null) {
            logCapture.detach();
        }
    }

    @Test
    @DisplayName("답변 등록 이벤트를 받으면 리뷰 작성자 앞으로 해당 리뷰를 가리키는 알림을 적재한다")
    void storesNotificationForReviewer() {
        shopQueryDao.shopName = SHOP_NAME;

        listener.handle(event());

        assertThat(notificationService.notified).containsExactly(
            new Notified(REVIEWER_MEMBER_ID, REVIEW_ID, SHOP_NAME)
        );
    }

    @Test
    @DisplayName("가게명 조회가 비어 있어도 알림을 적재한다 — 문구 대체는 도메인 문구 소유자가 판단한다")
    void storesNotificationEvenWhenShopNameMissing() {
        shopQueryDao.shopName = null;

        listener.handle(event());

        assertThat(notificationService.notified).containsExactly(
            new Notified(REVIEWER_MEMBER_ID, REVIEW_ID, null)
        );
    }

    @Test
    @DisplayName("적재 결과를 식별자와 함께 로그로 남긴다")
    void logsStoredNotification() {
        logCapture = ListenerLogCapture.attachTo(ReviewOwnerReplyEventListener.class);
        shopQueryDao.shopName = SHOP_NAME;

        listener.handle(event());

        assertThat(logCapture.events())
            .singleElement()
            .extracting(ILoggingEvent::getFormattedMessage)
            .asString()
            .contains(String.valueOf(REVIEW_ID.value()))
            .contains(String.valueOf(REVIEWER_MEMBER_ID.value()))
            .contains(String.valueOf(OWNER_REPLY_ID.value()));
    }

    private static ReviewOwnerReplyCreatedEvent event() {
        return new ReviewOwnerReplyCreatedEvent(
            REVIEW_ID,
            REVIEWER_MEMBER_ID,
            SHOP_ID,
            OWNER_REPLY_ID,
            LocalDateTime.of(2026, 6, 20, 14, 3)
        );
    }

    private record Notified(MemberId memberId, ReviewId reviewId, String shopName) {
    }

    private static final class RecordingNotificationService extends NotificationService {
        private final List<Notified> notified = new ArrayList<>();
        private long sequence = 0L;

        private RecordingNotificationService() {
            super(null);
        }

        @Override
        public Long notifyReviewOwnerReply(MemberId reviewerMemberId, ReviewId reviewId, String shopName) {
            notified.add(new Notified(reviewerMemberId, reviewId, shopName));
            return ++sequence;
        }
    }

    private static final class StubShopQueryDao extends ShopQueryDao {
        private String shopName;

        private StubShopQueryDao() {
            super(null, null);
        }

        @Override
        public Optional<String> findShopName(Long shopId) {
            return Optional.ofNullable(shopName);
        }
    }
}
