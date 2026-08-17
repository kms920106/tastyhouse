package com.tastyhouse.domain.review.service;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.event.ReviewBlindApprovedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * 게시중단 요청 워크플로 불변식 단위 테스트.
 *
 * <p>봉인 대상은 스펙의 세 규칙이다 — <b>1회 제한</b>(단 CANCELED는 예외), <b>고객 동의 삭제</b>,
 * <b>타인 리뷰 접근 차단</b>. 승인 시각을 파라미터로 받는 설계 덕에 시계를 조작하지 않고 재노출 기한을
 * 고정해 검증할 수 있다.
 *
 * <p>write 포트·이벤트 발행 포트를 fake로 대체해 Spring/DB 없이 판정 로직만 검증한다.
 */
class ReviewBlindRequestServiceTest {

    private static final Long SHOP_ID = 1L;
    private static final Long CEO_ID = 7L;
    private static final Long REVIEWER_MEMBER_ID = 42L;
    private static final Long OTHER_MEMBER_ID = 99L;
    private static final Long REVIEW_ID = 100L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);

    private FakeReviewRepository reviewRepository;
    private FakeReviewBlindRequestRepository reviewBlindRequestRepository;
    private FakeReviewBlindRequestAttachmentRepository attachmentRepository;
    private FakeDomainEventPublisher domainEventPublisher;
    private FakeShopRequestIndexRepository shopRequestIndexRepository;
    private ReviewBlindRequestService reviewBlindRequestService;

    @BeforeEach
    void setUp() {
        reviewRepository = new FakeReviewRepository();
        reviewBlindRequestRepository = new FakeReviewBlindRequestRepository();
        attachmentRepository = new FakeReviewBlindRequestAttachmentRepository();
        domainEventPublisher = new FakeDomainEventPublisher();
        shopRequestIndexRepository = new FakeShopRequestIndexRepository();

        ReviewLifecycleService reviewLifecycleService = new ReviewLifecycleService(
            reviewRepository,
            new FakeReviewImageRepository(),
            new FakeReviewTagRepository(),
            new FakeReviewLikeRepository(),
            new FakeTagRepository(),
            domainEventPublisher
        );

        reviewBlindRequestService = new ReviewBlindRequestService(
            reviewBlindRequestRepository,
            attachmentRepository,
            reviewRepository,
            reviewLifecycleService,
            new ShopRequestIndexRecorder(shopRequestIndexRepository),
            domainEventPublisher
        );

        saveReview();
    }

    private void saveReview() {
        reviewRepository.save(Review.reconstitute(
            REVIEW_ID,
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
            NOW.minusDays(1)
        ));
    }

    private Long request() {
        return reviewBlindRequestService.request(
            SHOP_ID, REVIEW_ID, CEO_ID, ReviewBlindReason.PROFANITY, null, null
        );
    }

    @Nested
    @DisplayName("1회 제한")
    class OnceOnly {

        @Test
        @DisplayName("종결된 요청이 있으면 재신청할 수 없다 — 승인 후")
        void cannotRequestAgainAfterApproved() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);

            BusinessException exception = catchThrowableOfType(
                ReviewBlindRequestServiceTest.this::request, BusinessException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_USED);
        }

        @Test
        @DisplayName("종결된 요청이 있으면 재신청할 수 없다 — 반려 후")
        void cannotRequestAgainAfterRejected() {
            Long requestId = request();
            reviewBlindRequestService.reject(requestId, "위반 사실이 확인되지 않습니다.");

            BusinessException exception = catchThrowableOfType(
                ReviewBlindRequestServiceTest.this::request, BusinessException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_USED);
        }

        @Test
        @DisplayName("취소한 건은 1회를 소진하지 않아 재신청할 수 있다")
        void canRequestAgainAfterCanceled() {
            Long requestId = request();
            reviewBlindRequestService.cancel(requestId, SHOP_ID);

            assertThatCode(ReviewBlindRequestServiceTest.this::request).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("대기중인 요청이 있으면 중복 접수를 막는다(1회 제한과 구분되는 에러코드)")
        void cannotRequestWhilePending() {
            request();

            BusinessException exception = catchThrowableOfType(
                ReviewBlindRequestServiceTest.this::request, BusinessException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_PENDING);
        }
    }

    @Nested
    @DisplayName("승인")
    class Approve {

        @Test
        @DisplayName("승인하면 재노출 기한이 승인 시각 + 30일로 설정되고 리뷰가 숨겨진다")
        void approveSetsBlindUntilAndHidesReview() {
            Long requestId = request();

            reviewBlindRequestService.approve(requestId, NOW);

            ReviewBlindRequest saved = reviewBlindRequestRepository
                .findById(com.tastyhouse.domain.review.vo.ReviewBlindRequestId.of(requestId))
                .orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(ReviewBlindStatus.APPROVED);
            assertThat(saved.getBlindUntil())
                .isEqualTo(NOW.plusDays(ReviewBlindRequest.BLIND_PERIOD_DAYS));
            assertThat(reviewRepository.findById(ReviewId.of(REVIEW_ID)).orElseThrow().isHidden()).isTrue();
        }

        @Test
        @DisplayName("승인하면 리뷰 작성자에게 보낼 알림 이벤트를 발행한다")
        void approvePublishesEvent() {
            Long requestId = request();

            reviewBlindRequestService.approve(requestId, NOW);

            ReviewBlindApprovedEvent event = domainEventPublisher.publishedEvents().stream()
                .filter(ReviewBlindApprovedEvent.class::isInstance)
                .map(ReviewBlindApprovedEvent.class::cast)
                .findFirst()
                .orElseThrow();
            assertThat(event.reviewerMemberId()).isEqualTo(MemberId.of(REVIEWER_MEMBER_ID));
            assertThat(event.blindUntil()).isEqualTo(NOW.plusDays(ReviewBlindRequest.BLIND_PERIOD_DAYS));
        }
    }

    @Nested
    @DisplayName("고객 삭제 동의")
    class ConsentToDelete {

        @Test
        @DisplayName("동의하면 요청이 삭제 처리로 종결되고 리뷰가 삭제된다")
        void consentDeletesReview() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);

            reviewBlindRequestService.consentToDelete(
                ReviewId.of(REVIEW_ID), MemberId.of(REVIEWER_MEMBER_ID)
            );

            ReviewBlindRequest saved = reviewBlindRequestRepository
                .findById(com.tastyhouse.domain.review.vo.ReviewBlindRequestId.of(requestId))
                .orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(ReviewBlindStatus.DELETED);
            assertThat(saved.getBlindUntil()).isNull();
            assertThat(reviewRepository.findById(ReviewId.of(REVIEW_ID))).isEmpty();
        }

        @Test
        @DisplayName("타인의 리뷰에 동의하면 404다 — 게시중단된 비공개 리뷰라 존재를 숨긴다")
        void cannotConsentToOthersReview() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);

            ResourceNotFoundException exception = catchThrowableOfType(
                () -> reviewBlindRequestService.consentToDelete(
                    ReviewId.of(REVIEW_ID), MemberId.of(OTHER_MEMBER_ID)
                ),
                ResourceNotFoundException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
            assertThat(reviewRepository.findById(ReviewId.of(REVIEW_ID))).isPresent();
        }

        @Test
        @DisplayName("게시중단 상태가 아니면 동의할 수 없다")
        void cannotConsentWhenNotApproved() {
            request();

            BusinessException exception = catchThrowableOfType(
                () -> reviewBlindRequestService.consentToDelete(
                    ReviewId.of(REVIEW_ID), MemberId.of(REVIEWER_MEMBER_ID)
                ),
                BusinessException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED);
        }

        @Test
        @DisplayName("삭제 거부는 아무 전이도 하지 않는다 — 30일 배치가 처리한다")
        void rejectDeletionDoesNotTransition() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);

            reviewBlindRequestService.rejectDeletion(
                ReviewId.of(REVIEW_ID), MemberId.of(REVIEWER_MEMBER_ID)
            );

            ReviewBlindRequest saved = reviewBlindRequestRepository
                .findById(com.tastyhouse.domain.review.vo.ReviewBlindRequestId.of(requestId))
                .orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(ReviewBlindStatus.APPROVED);
            assertThat(reviewRepository.findById(ReviewId.of(REVIEW_ID))).isPresent();
        }
    }

    @Nested
    @DisplayName("만료 재노출")
    class Expire {

        @Test
        @DisplayName("만료하면 요청이 재노출 상태가 되고 리뷰 숨김이 풀린다")
        void expireUnhidesReview() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);

            reviewBlindRequestService.expire(requestId);

            ReviewBlindRequest saved = reviewBlindRequestRepository
                .findById(com.tastyhouse.domain.review.vo.ReviewBlindRequestId.of(requestId))
                .orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(ReviewBlindStatus.EXPIRED);
            assertThat(reviewRepository.findById(ReviewId.of(REVIEW_ID)).orElseThrow().isHidden()).isFalse();
        }

        @Test
        @DisplayName("기한이 지난 건만 만료 대상으로 조회된다")
        void findsOnlyExpiredBlinds() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);
            LocalDateTime blindUntil = NOW.plusDays(ReviewBlindRequest.BLIND_PERIOD_DAYS);

            assertThat(reviewBlindRequestService.findExpirableBlinds(blindUntil.minusSeconds(1))).isEmpty();
            assertThat(reviewBlindRequestService.findExpirableBlinds(blindUntil)).hasSize(1);
        }

        @Test
        @DisplayName("리뷰가 이미 삭제됐어도 요청은 종결된다 — 다음 주기에 같은 건을 다시 집지 않는다")
        void expireSucceedsWhenReviewAlreadyDeleted() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);
            reviewRepository.deleteById(ReviewId.of(REVIEW_ID));

            assertThatCode(() -> reviewBlindRequestService.expire(requestId)).doesNotThrowAnyException();

            ReviewBlindRequest saved = reviewBlindRequestRepository
                .findById(com.tastyhouse.domain.review.vo.ReviewBlindRequestId.of(requestId))
                .orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(ReviewBlindStatus.EXPIRED);
        }
    }

    @Nested
    @DisplayName("요청처리 현황 인덱스 동기화")
    class IndexSync {

        /**
         * 원본 상태 → 통합 상태 매핑은 컨텍스트 경계 때문에 recorder가 아니라 이 서비스가 소유한다.
         * 신규 전이 2종이 종결(APPROVED)로 접히는지 봉인한다 — 목록에 "재노출"·"삭제"라는 없는 통합
         * 상태가 새어 나가면 안 된다.
         */
        @Test
        @DisplayName("만료 재노출은 통합 현황에서 종결(승인)로 보인다")
        void expiredMapsToApproved() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);

            reviewBlindRequestService.expire(requestId);

            assertThat(shopRequestIndexRepository.require(ShopRequestType.REVIEW_BLIND, requestId).getStatus())
                .isEqualTo(ShopRequestStatus.APPROVED);
        }

        @Test
        @DisplayName("고객 동의 삭제도 통합 현황에서 종결(승인)로 보인다")
        void deletedMapsToApproved() {
            Long requestId = request();
            reviewBlindRequestService.approve(requestId, NOW);

            reviewBlindRequestService.consentToDelete(
                ReviewId.of(REVIEW_ID), MemberId.of(REVIEWER_MEMBER_ID)
            );

            assertThat(shopRequestIndexRepository.require(ShopRequestType.REVIEW_BLIND, requestId).getStatus())
                .isEqualTo(ShopRequestStatus.APPROVED);
        }

        @Test
        @DisplayName("반려는 통합 현황에도 반려로 기록되고 사유가 남는다")
        void rejectedMapsToRejected() {
            Long requestId = request();

            reviewBlindRequestService.reject(requestId, "위반 사실이 확인되지 않습니다.");

            ShopRequestIndex index = shopRequestIndexRepository.require(ShopRequestType.REVIEW_BLIND, requestId);
            assertThat(index.getStatus()).isEqualTo(ShopRequestStatus.REJECTED);
            assertThat(index.getRejectReason()).isEqualTo("위반 사실이 확인되지 않습니다.");
        }
    }

    @Nested
    @DisplayName("증빙 서류 첨부")
    class Attachments {

        @Test
        @DisplayName("첨부 파일에 1부터 순번이 부여된다")
        void attachmentsGetSequentialSort() {
            reviewBlindRequestService.request(
                SHOP_ID, REVIEW_ID, CEO_ID, ReviewBlindReason.PRIVACY, null, List.of(11L, 22L, 33L)
            );

            assertThat(attachmentRepository.saved())
                .extracting(attachment -> attachment.getAttachmentFileId().value(), ReviewBlindRequestAttachment::getSort)
                .containsExactly(
                    org.assertj.core.api.Assertions.tuple(11L, 1),
                    org.assertj.core.api.Assertions.tuple(22L, 2),
                    org.assertj.core.api.Assertions.tuple(33L, 3)
                );
        }

        @Test
        @DisplayName("첨부가 없으면 아무것도 적재하지 않는다")
        void noAttachmentsSavedWhenEmpty() {
            request();

            assertThat(attachmentRepository.saved()).isEmpty();
        }
    }
}
