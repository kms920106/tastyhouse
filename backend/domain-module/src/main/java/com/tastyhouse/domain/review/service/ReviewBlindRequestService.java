package com.tastyhouse.domain.review.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.event.ReviewBlindApprovedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestAttachmentRepository;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 리뷰 게시중단 요청 워크플로 불변식(도메인 서비스).
 *
 * <p>"점주 요청 → 관리자 심사 → 승인 시 리뷰 숨김 → 30일 뒤 재노출 또는 고객 동의 시 삭제"라는 워크플로의
 * 규칙은 요청자(ceo)·심사자(admin)·고객(web)·배치가 서로 다른 액터임에도 동일하게 유지되어야 한다.
 * 특히 승인({@link #approve})은 <b>요청 애그리거트의 상태 전이와 리뷰의 숨김 반영이 한 트랜잭션에서
 * 반드시 함께</b> 일어나야 하는 원자 연산이다 — 둘 중 하나만 반영되면 "승인됐는데 리뷰가 계속 노출되는"
 * 상태가 남는다({@code ShopImageApprovalService}가 이미지 교체에 대해 갖는 것과 같은 성질).
 *
 * <p><b>{@link ShopRequestIndexRecorder}를 생성자 필수 의존으로 받는다</b> — 요청처리 현황
 * ({@code SHOP_REQUEST_INDEX})은 파생 읽기모델이고 기록이 누락되면 그 요청이 통합 목록에서 아예 보이지
 * 않는다. 필수 의존으로 두면 새 상태 전이 메서드를 추가할 때 동기화 배선이 필요하다는 사실이 컴파일
 * 단계에서 드러난다.
 *
 * <p>취소({@link #cancel(Long, Long)})는 원본 애그리거트의 상태 전이이며, 취소 후에는 같은 리뷰에
 * 재요청이 가능해진다 — PENDING 중복 차단과 1회 제한이 모두 상태 조회에 기반하므로 코드 추가 없이
 * 자동으로 풀린다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ReviewDomainConfig}가 담당한다. 도메인 모델은 POJO라 더티 체킹이 없으므로 변경 후 명시적으로
 * {@code save}를 호출한다.
 */
public class ReviewBlindRequestService {

    private final ReviewBlindRequestRepository reviewBlindRequestRepository;
    private final ReviewBlindRequestAttachmentRepository reviewBlindRequestAttachmentRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLifecycleService reviewLifecycleService;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewBlindRequestService(
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ReviewBlindRequestAttachmentRepository reviewBlindRequestAttachmentRepository,
        ReviewRepository reviewRepository,
        ReviewLifecycleService reviewLifecycleService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewBlindRequestRepository = reviewBlindRequestRepository;
        this.reviewBlindRequestAttachmentRepository = reviewBlindRequestAttachmentRepository;
        this.reviewRepository = reviewRepository;
        this.reviewLifecycleService = reviewLifecycleService;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 점주가 리뷰 게시중단을 요청한다.
     *
     * <p>대상 리뷰가 그 가게의 것인지 역조회로 재검증한다 — 경로의 {@code shopId}만 믿으면 남의 가게
     * 리뷰에 게시중단을 걸 수 있는 IDOR이 된다.
     *
     * <p><b>같은 리뷰는 1회만 요청할 수 있다.</b> 대기중 중복(PENDING) 검사에 더해 종결 요청 존재 여부를
     * 함께 검사한다({@code REVIEW_BLIND_REQUEST_ALREADY_USED}).
     *
     * <p><b>⚠️ 이 1회 제한은 물리 제약으로 막을 수 없다.</b> DDL 주석이 이미 밝히듯 PENDING 중복조차
     * UNIQUE로 막을 수 없고(MySQL 부분 인덱스 미지원), {@code UNIQUE(review_id)}를 걸면 CANCELED 후
     * 재요청까지 막혀 취소 규칙과 충돌한다. 따라서 이 애플리케이션 검사가 유일한 차단 수단이며 동시
     * 요청에는 이론상 뚫린다 — {@code CEO_REPLY_PHRASE}의 5개 상한이 같은 이유로 의도적으로 방치된 것과
     * 같은 판단이다(심사자가 중복 건을 볼 뿐 금액·주문에 영향이 없다).
     *
     * <p>같은 트랜잭션에서 {@link ShopRequestIndexRecorder#record}를 호출해 요청처리 현황에 기록한다.
     * 누락되면 그 요청이 통합 목록에서 보이지 않는다.
     *
     * @param attachmentFileIds 증빙 서류 파일 식별자 목록(선택). 개수 상한은 요청 DTO가 검증한다
     * @return 생성된 요청 식별자
     */
    public Long request(
        Long shopId,
        Long reviewId,
        Long ceoId,
        ReviewBlindReason reason,
        String detailReason,
        List<Long> attachmentFileIds
    ) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        loadReviewOfShop(targetReviewId, shopId);
        validateDetailReason(reason, detailReason);

        if (reviewBlindRequestRepository.existsByReviewIdAndStatus(targetReviewId, ReviewBlindStatus.PENDING)) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_PENDING);
        }
        if (reviewBlindRequestRepository.existsTerminatedByReviewId(targetReviewId)) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_USED);
        }

        ReviewBlindRequest saved = reviewBlindRequestRepository.save(
            ReviewBlindRequest.of(targetReviewId, ShopId.of(shopId), CeoId.of(ceoId), reason, detailReason)
        );

        saveAttachments(saved.getId(), attachmentFileIds);

        shopRequestIndexRecorder.record(
            ShopId.of(shopId),
            ShopRequestType.REVIEW_BLIND,
            saved.getId(),
            describe(reason, reviewId),
            null,
            ceoId
        );
        return saved.getId();
    }

    /**
     * 관리자가 요청을 승인하고, 대상 리뷰를 즉시 숨긴다(원자 연산).
     *
     * <p>승인 시각으로부터 {@link ReviewBlindRequest#BLIND_PERIOD_DAYS}일 뒤를 재노출 예정일시로 설정한다.
     * 시각을 파라미터로 받는 이유는 {@link ReviewBlindRequest#approve} Javadoc 참고.
     *
     * <p>인덱스 동기화를 <b>리뷰 숨김이 끝난 뒤 마지막에</b> 한다. 지금은 전체가 한 트랜잭션이라 순서를
     * 바꿔도 결과가 같지만, 인덱스는 "요청이 어떻게 처리됐는가"를 답하는 기록이므로 승인이 실제로 반영된
     * 뒤에 APPROVED가 되는 순서가 그 의미와 맞는다.
     *
     * <p>리뷰 작성자에게 보낼 알림은 {@link ReviewBlindApprovedEvent}로 발행한다 — 알림 적재가 실패해도
     * 게시중단 승인 자체는 유효하므로 AFTER_COMMIT 리스너가 처리한다.
     *
     * @param now 승인 시각. 도메인이 {@code LocalDateTime.now()}를 직접 호출하지 않도록 호출부가 넘긴다
     */
    public void approve(Long requestId, LocalDateTime now) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.approve(now.plusDays(ReviewBlindRequest.BLIND_PERIOD_DAYS));
        request = reviewBlindRequestRepository.save(request);

        Review review = reviewRepository.findById(request.getReviewId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        review.hide();
        reviewRepository.save(review);

        domainEventPublisher.publish(ReviewBlindApprovedEvent.of(
            request.getReviewId(),
            review.getMemberId(),
            ReviewBlindRequestId.of(request.getId()),
            request.getBlindUntil(),
            now
        ));

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    /**
     * 관리자가 요청을 반려한다. 리뷰는 노출 상태를 유지한다.
     */
    public void reject(Long requestId, String rejectReason) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        request = reviewBlindRequestRepository.save(request);

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), rejectReason);
    }

    /**
     * 점주가 대기중인 요청을 취소한다.
     *
     * <p>요청이 다른 가게 것이면 취소를 거부한다 — 경로의 {@code shopId}로 소유권을 통과했더라도
     * {@code requestId}는 별개의 하위 리소스다.
     */
    public void cancel(Long requestId, Long shopId) {
        ReviewBlindRequest request = loadRequest(requestId);
        if (!request.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND);
        }
        request.cancel();
        request = reviewBlindRequestRepository.save(request);

        shopRequestIndexRecorder.syncCanceled(ShopRequestType.REVIEW_BLIND, request.getId());
    }

    /**
     * 고객이 자기 리뷰의 삭제에 동의한다. 리뷰를 삭제하고 요청을 {@code DELETED}로 종결한다.
     *
     * <p><b>작성자 본인 검증을 먼저 한다</b> — 경로의 {@code reviewId}만 믿으면 남의 리뷰를 삭제할 수 있는
     * IDOR이 된다.
     *
     * <p><b>불일치를 403이 아니라 404({@code REVIEW_NOT_FOUND})로 응답하는 것은 의도적이다.</b> 사장님
     * 답변 경로는 같은 불일치를 403({@code SHOP_ACCESS_DENIED})으로 내는데, 그 판단 근거는 "리뷰는 web에
     * 공개된 리소스라 존재 자체가 비밀이 아니다"였다. 그러나 이 엔드포인트의 대상은 <b>이미 게시중단된
     * 비공개 리뷰</b>이므로 존재를 숨기는 쪽이 맞다.
     *
     * <p>삭제는 {@link ReviewLifecycleService#removeOwnedBy}를 재사용해 사진·태그 정리와 삭제 이벤트 발행이
     * 함께 일어나게 한다 — 직접 {@code deleteById}만 부르면 그 후처리가 누락된다. (리뷰 도메인에는 소프트
     * 삭제가 없고 하드 삭제다.)
     *
     * <p><b>요청 종결을 리뷰 삭제보다 먼저 한다</b> — 삭제 후에는 {@code review}가 사라져 그 값을 읽을 수
     * 없고, 인덱스 동기화가 참조하는 요청 식별자도 삭제와 무관해야 하기 때문이다.
     */
    public void consentToDelete(ReviewId reviewId, MemberId memberId) {
        Review review = loadOwnedReview(reviewId, memberId);

        ReviewBlindRequest request = reviewBlindRequestRepository.findApprovedByReviewId(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED));

        request.deleteByConsent();
        request = reviewBlindRequestRepository.save(request);

        reviewLifecycleService.removeOwnedBy(reviewId, memberId, review.getProductId());

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    /**
     * 고객이 삭제를 거부한다.
     *
     * <p><b>아무 전이도 하지 않는다</b> — 30일 배치가 재노출을 처리한다. 거부를 기록하지 않는 이유는 원문이
     * "동의하지 않으면 30일 뒤 재노출"만 규정하고 거부 이력을 요구하지 않기 때문이다. 그럼에도 엔드포인트를
     * 두는 것은 고객이 안내를 확인하고 닫을 수 있어야 하고, 그 시점에 대상이 실제로 게시중단 상태인지
     * 검증해 잘못된 안내(이미 만료된 건에 대한 동의 요청 등)를 드러내기 위함이다.
     */
    public void rejectDeletion(ReviewId reviewId, MemberId memberId) {
        loadOwnedReview(reviewId, memberId);

        reviewBlindRequestRepository.findApprovedByReviewId(reviewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED));
    }

    /**
     * 배치가 만료분을 재노출한다 — 요청을 {@code EXPIRED}로 종결하고 리뷰의 숨김을 해제한다.
     *
     * <p><b>재노출 전 리뷰 존재를 재확인한다</b> — 조회 시점과 처리 시점 사이에 리뷰가 삭제됐을 수 있다.
     * 이미 없으면 요청만 종결하고 넘어간다(없는 리뷰를 되살릴 수는 없고, 요청을 APPROVED로 남겨 두면
     * 다음 주기마다 같은 건을 다시 집어 든다).
     */
    public void expire(Long requestId) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.expire();
        request = reviewBlindRequestRepository.save(request);

        reviewRepository.findById(request.getReviewId()).ifPresent(review -> {
            review.unhide();
            reviewRepository.save(review);
        });

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), toShopRequestStatus(request.getStatus()), null);
    }

    /**
     * 재노출 기한이 지난 게시중단 건을 조회한다(배치 진입점).
     */
    public List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now) {
        return reviewBlindRequestRepository.findExpirableBlinds(now);
    }

    /**
     * 게시중단 처리 상태 → 요청처리 현황의 통합 상태.
     *
     * <p><b>이 매핑이 여기 있는 이유는 컨텍스트 경계다.</b> shop 쪽 {@link ShopRequestIndexRecorder}가
     * {@link ReviewBlindStatus}를 받으면 shop → review.model 위반이고, 반대로 그 enum이
     * {@link ShopRequestStatus}를 알면 review → shop.model 위반이다({@code ContextBoundaryTest}가 둘 다
     * 잡는다). 두 컨텍스트를 모두 손에 든 이 서비스가 변환을 소유하는 것이 유일하게 경계를 지키는 배치다
     * (이미지 변경·조정 신청은 상태 enum이 둘 다 shop 소유라 recorder가 직접 매핑한다).
     *
     * <p><b>{@code EXPIRED}/{@code DELETED}는 {@code APPROVED}로 접는다</b> — 게시중단 요청 자체는
     * 승인됐고, 그 이후의 생애주기는 리뷰 쪽 사정이다. 통합 현황은 "내가 낸 요청이 어떻게 처리됐는가"를
     * 답하는 화면이므로 둘 다 종결(승인)로 보이면 된다.
     *
     * <p>값을 열거하는 switch로 쓴다 — 상수가 추가되면 컴파일이 깨져 누락이 드러나야 한다.
     */
    private static ShopRequestStatus toShopRequestStatus(ReviewBlindStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case APPROVED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
            case EXPIRED -> ShopRequestStatus.APPROVED;
            case DELETED -> ShopRequestStatus.APPROVED;
        };
    }

    /**
     * 증빙 서류를 요청에 첨부한다. 순번은 전달된 순서를 1부터 부여한다({@code ReviewImage}와 동형).
     */
    private void saveAttachments(Long blindRequestId, List<Long> attachmentFileIds) {
        if (attachmentFileIds == null || attachmentFileIds.isEmpty()) {
            return;
        }

        ReviewBlindRequestId requestId = ReviewBlindRequestId.of(blindRequestId);
        List<ReviewBlindRequestAttachment> attachments = new ArrayList<>();
        for (int i = 0; i < attachmentFileIds.size(); i++) {
            attachments.add(ReviewBlindRequestAttachment.of(
                requestId,
                UploadedFileId.of(attachmentFileIds.get(i)),
                i + 1
            ));
        }
        reviewBlindRequestAttachmentRepository.saveAll(attachments);
    }

    /**
     * {@code ETC} 사유는 상세 내용이 필수다 — 사유 코드만으로는 심사자가 판단할 근거가 없다.
     */
    private void validateDetailReason(ReviewBlindReason reason, String detailReason) {
        if (reason == ReviewBlindReason.ETC && (detailReason == null || detailReason.isBlank())) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_DETAIL_REASON_REQUIRED);
        }
    }

    /**
     * 요청을 한 줄로 요약한다(예: {@code "리뷰 게시중단 요청 - 광고·홍보(리뷰 #482)"}).
     */
    private String describe(ReviewBlindReason reason, Long reviewId) {
        return ShopRequestType.REVIEW_BLIND.getDescription() + " - " + reason.getDescription()
            + "(리뷰 #" + reviewId + ")";
    }

    private ReviewBlindRequest loadRequest(Long requestId) {
        return reviewBlindRequestRepository.findById(ReviewBlindRequestId.of(requestId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_FOUND));
    }

    /**
     * 리뷰가 대상 가게의 것임을 재검증한다.
     */
    private void loadReviewOfShop(ReviewId reviewId, Long shopId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getShopId().equals(ShopId.of(shopId))) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
    }

    /**
     * 리뷰가 인증 주체 본인의 것임을 재검증한다. 불일치는 존재를 숨기기 위해 404다(위 Javadoc 참고).
     */
    private Review loadOwnedReview(ReviewId reviewId, MemberId memberId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getMemberId().equals(memberId)) {
            throw new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return review;
    }
}
