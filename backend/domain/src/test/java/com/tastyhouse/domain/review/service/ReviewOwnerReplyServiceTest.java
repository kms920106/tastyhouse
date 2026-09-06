package com.tastyhouse.domain.review.service;

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
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.event.ReviewOwnerReplyCreatedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.repository.ReviewOwnerReplyRepository;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 사장님 답변 불변식 단위 테스트 — 특히 <b>30일 작성 제한이 등록에만 걸리는지</b>를 봉인한다.
 *
 * <p>기한 판정 기준일을 파라미터로 받는 설계 덕에 시계를 조작하지 않고 29·30·31일차를 직접 지정할 수
 * 있다. 도메인이 {@code LocalDate.now()}를 직접 부르면 이 테스트 자체가 불가능하다.
 *
 * <p>write 포트·금칙어 포트·이벤트 발행 포트를 fake로 대체해 Spring/DB 없이 판정 로직만 검증한다.
 */
class ReviewOwnerReplyServiceTest {

    private static final Long SHOP_ID = 1L;
    private static final Long CEO_ID = 7L;
    private static final Long REVIEWER_MEMBER_ID = 42L;
    private static final String CONTENT = "소중한 리뷰 감사합니다.";

    /** 리뷰 작성일 — 모든 기한 계산의 기준. 마감일은 이 날짜 + 30일이다. */
    private static final LocalDate REVIEW_CREATED_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDate DEADLINE = REVIEW_CREATED_DATE.plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);

    private FakeReviewOwnerReplyRepository reviewOwnerReplyRepository;
    private FakeDomainEventPublisher domainEventPublisher;
    private ReviewOwnerReplyService reviewOwnerReplyService;

    private Long reviewId;

    @BeforeEach
    void setUp() {
        FakeReviewRepository reviewRepository = new FakeReviewRepository();
        reviewOwnerReplyRepository = new FakeReviewOwnerReplyRepository();
        domainEventPublisher = new FakeDomainEventPublisher();
        reviewOwnerReplyService = new ReviewOwnerReplyService(
            reviewOwnerReplyRepository,
            reviewRepository,
            new ProhibitedWordValidator(new FakeProhibitedWordRepository()),
            domainEventPublisher
        );

        // 식별자를 채운 상태로 넣는다 — fake의 save는 id가 있으면 그대로 보존하므로 createdAt(기한 판정의
        // 기준)이 유실되지 않는다. id가 null이면 fake가 재구성하면서 createdAt을 채우지 않는다.
        reviewId = 100L;
        reviewRepository.save(Review.reconstitute(
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
            .isEqualTo(ErrorCode.REVIEW_OWNER_REPLY_PERIOD_EXPIRED);
    }

    @Test
    @DisplayName("기한이 지난 뒤에도 이미 등록된 답변은 수정할 수 있다 — 제한 대상은 '작성'뿐이다")
    void modifySucceedsAfterDeadline() {
        reviewOwnerReplyService.register(SHOP_ID, reviewId, CEO_ID, CONTENT, DEADLINE);

        assertThatCode(() -> reviewOwnerReplyService.modify(SHOP_ID, reviewId, "오타를 고쳤습니다."))
            .doesNotThrowAnyException();
        assertThat(reviewOwnerReplyRepository.findByReviewId(ReviewId.of(reviewId)))
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
        assertThat(reviewOwnerReplyRepository.findByReviewId(ReviewId.of(reviewId))).isEmpty();
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
            .isEqualTo(ErrorCode.REVIEW_OWNER_REPLY_PERIOD_EXPIRED);
    }

    /**
     * 사장님 답변 write 포트의 인메모리 fake. {@code save}가 신규 저장 시 식별자를 채운 새 인스턴스를
     * 반환하는 것까지 실제 어댑터와 같게 재현한다.
     */
    private static class FakeReviewOwnerReplyRepository implements ReviewOwnerReplyRepository {

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

    /**
     * 금칙어 테이블을 대신하는 fake. 시드와 동일하게 "전화주문" 하나만 담는다.
     */
    private static class FakeProhibitedWordRepository implements ProhibitedWordRepository {

        @Override
        public List<ProhibitedWord> findAll() {
            return List.of(ProhibitedWord.reconstitute(1L, "전화주문", "전화 주문 유도"));
        }
    }
}
