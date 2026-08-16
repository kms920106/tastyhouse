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

/**
 * {@link ReviewOwnerReplyEventListener}의 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>이 리스너는 review↔notification 두 컨텍스트를 잇는 지점이므로, 검증 대상은 로깅이 아니라
 * <b>답변 등록 이벤트가 리뷰 작성자 앞으로 알림을 적재하는가</b>이다. 특히 수신자가
 * {@code reviewerMemberId}(작성자)여야 하고 이동 대상이 그 리뷰여야 한다.
 *
 * <p>가게명은 리스너가 {@link ShopQueryDao}로 조회해 문구 조립에 넘기므로, 조회가 비어 있는 경우까지
 * 함께 봉인한다 — 알림 본문에 "null 사장님"이 새는 것을 막기 위함이다.
 */
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

    /**
     * 적재 호출만 기록하는 스텁. {@code NotificationService}는 인터페이스가 아니라 클래스이므로 상속으로
     * 대체하며, 부모 생성자가 요구하는 포트는 호출되지 않으므로 {@code null}을 넘긴다.
     */
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

    /**
     * 가게명 조회만 대신하는 스텁. 위와 같은 이유로 상속을 쓴다.
     */
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
