package com.tastyhouse.application.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.event.ReviewOwnerReplyCreatedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyPersistencePort;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordPersistencePort;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.testsupport.review.service.FakeDomainEventPublisher;
import com.tastyhouse.testsupport.review.service.FakeReviewPersistencePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewOwnerReplyServiceTest {

    private static final Long SHOP_ID = 1L;
    private static final Long CEO_ID = 7L;
    private static final Long REVIEWER_MEMBER_ID = 42L;
    private static final String CONTENT = "소중한 리뷰 감사합니다.";

    private static final LocalDate REVIEW_CREATED_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDate DEADLINE = REVIEW_CREATED_DATE.plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);

    private FakeReviewOwnerReplyPersistencePort reviewOwnerReplyPersistencePort;
    private FakeDomainEventPublisher domainEventPublisher;
    private ReviewOwnerReplyService reviewOwnerReplyService;

    private Long reviewId;

    @BeforeEach
    void setUp() {
        FakeReviewPersistencePort reviewPersistencePort = new FakeReviewPersistencePort();
        reviewOwnerReplyPersistencePort = new FakeReviewOwnerReplyPersistencePort();
        domainEventPublisher = new FakeDomainEventPublisher();
        reviewOwnerReplyService = new ReviewOwnerReplyService(
            reviewOwnerReplyPersistencePort,
            reviewPersistencePort,
            new ProhibitedWordValidator(new FakeProhibitedWordPersistencePort()),
            domainEventPublisher
        );

        reviewId = 100L;
        reviewPersistencePort.save(Review.reconstitute(
            reviewId,
            ShopId.of(SHOP_ID),
            null,
            MemberId.of(REVIEWER_MEMBER_ID),
            "국물이 진하고 맛있었어요.",
            4.5, 4.5, 4.0, 4.0, 4.5, 5.0, 4.5,
            true,
            null,
            false,
            false,
            null,
            null,
            REVIEW_CREATED_DATE.atTime(20, 11)
        ));
    }

    @Test
    @DisplayName("리뷰 작성일 + 29일에는 답변을 등록할 수 있다")
    void registerSucceedsOnDayBeforeDeadline() {
        LocalDate today = REVIEW_CREATED_DATE.plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS - 1);

        Long replyId = reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, CONTENT, today);

        assertThat(replyId).isNotNull();
    }

    @Test
    @DisplayName("마감일 당일(+30일)에도 답변을 등록할 수 있다 — 날짜 경계로 자르므로 그날은 하루 종일 가능하다")
    void registerSucceedsOnDeadlineDay() {
        Long replyId = reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, CONTENT, DEADLINE);

        assertThat(replyId).isNotNull();
    }

    @Test
    @DisplayName("마감일 다음날(+31일)에는 REVIEW_OWNER_REPLY_PERIOD_EXPIRED로 거부한다")
    void registerFailsAfterDeadline() {
        LocalDate today = DEADLINE.plusDays(1);

        assertThatThrownBy(() -> reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, CONTENT, today))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(CeoErrorCode.REVIEW_OWNER_REPLY_PERIOD_EXPIRED);
    }

    @Test
    @DisplayName("기한이 지난 뒤에도 이미 등록된 답변은 수정할 수 있다 — 제한 대상은 '작성'뿐이다")
    void modifySucceedsAfterDeadline() {
        reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, CONTENT, DEADLINE);

        assertThatCode(() -> reviewOwnerReplyService.modify(SHOP_ID, reviewId, "오타를 고쳤습니다."))
            .doesNotThrowAnyException();
        assertThat(reviewOwnerReplyPersistencePort.findByReviewId(ReviewId.of(reviewId)))
            .get()
            .extracting(ReviewOwnerReply::getContent)
            .isEqualTo("오타를 고쳤습니다.");
    }

    @Test
    @DisplayName("기한이 지난 뒤에도 이미 등록된 답변은 삭제할 수 있다")
    void removeSucceedsAfterDeadline() {
        reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, CONTENT, DEADLINE);

        assertThatCode(() -> reviewOwnerReplyService.remove(SHOP_ID, reviewId))
            .doesNotThrowAnyException();
        assertThat(reviewOwnerReplyPersistencePort.findByReviewId(ReviewId.of(reviewId))).isEmpty();
    }

    @Test
    @DisplayName("register만 ReviewOwnerReplyCreatedEvent를 발행한다 — modify·remove는 발행하지 않는다")
    void onlyRegisterPublishesEvent() {
        reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, CONTENT, DEADLINE);
        reviewOwnerReplyService.modify(SHOP_ID, reviewId, "오타를 고쳤습니다.");
        reviewOwnerReplyService.remove(SHOP_ID, reviewId);

        assertThat(domainEventPublisher.publishedEvents())
            .singleElement()
            .isInstanceOfSatisfying(ReviewOwnerReplyCreatedEvent.class, event -> {
                assertThat(event.reviewId()).isEqualTo(ReviewId.of(reviewId));
                assertThat(event.reviewerMemberId()).isEqualTo(MemberId.of(REVIEWER_MEMBER_ID));
                assertThat(event.shopId()).isEqualTo(ShopId.of(SHOP_ID));
                assertThat(event.ownerReplyId()).isNotNull();
                assertThat(event.occurredAt()).isNotNull();
            });
    }

    @Test
    @DisplayName("기한 만료는 금칙어 검수보다 먼저 판정한다 — 어차피 등록할 수 없는 요청에 검수를 돌리지 않는다")
    void periodIsCheckedBeforeProhibitedWord() {
        LocalDate today = DEADLINE.plusDays(1);

        assertThatThrownBy(() -> reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, "전화주문 하세요", today))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(CeoErrorCode.REVIEW_OWNER_REPLY_PERIOD_EXPIRED);
    }

    private static class FakeReviewOwnerReplyPersistencePort implements ReviewOwnerReplyPersistencePort {

        private final Map<Long, ReviewOwnerReply> replies = new HashMap<>();
        private long sequence = 0L;

        @Override
        public Optional<ReviewOwnerReply> findById(ReviewOwnerReplyId id) {
            return Optional.ofNullable(replies.get(id.value()));
        }

        @Override
        public Optional<ReviewOwnerReply> findByReviewId(ReviewId reviewId) {
            return replies.values().stream()
                .filter(reply -> reply.getReviewId().equals(reviewId))
                .findFirst();
        }

        @Override
        public boolean existsByReviewId(ReviewId reviewId) {
            return findByReviewId(reviewId).isPresent();
        }

        @Override
        public ReviewOwnerReply save(ReviewOwnerReply reviewOwnerReply) {
            if (reviewOwnerReply.getId() != null) {
                replies.put(reviewOwnerReply.getId(), reviewOwnerReply);
                return reviewOwnerReply;
            }

            ReviewOwnerReply persisted = ReviewOwnerReply.reconstitute(
                ++sequence,
                reviewOwnerReply.getReviewId(),
                reviewOwnerReply.getShopId(),
                reviewOwnerReply.getCeoId(),
                reviewOwnerReply.getContent(),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            replies.put(persisted.getId(), persisted);
            return persisted;
        }

        @Override
        public void delete(ReviewOwnerReply reviewOwnerReply) {
            replies.remove(reviewOwnerReply.getId());
        }
    }

    private static class FakeProhibitedWordPersistencePort implements ProhibitedWordPersistencePort {

        @Override
        public List<ProhibitedWord> findAll() {
            return List.of(ProhibitedWord.reconstitute(1L, "전화주문", "전화 주문 유도"));
        }
    }
}
