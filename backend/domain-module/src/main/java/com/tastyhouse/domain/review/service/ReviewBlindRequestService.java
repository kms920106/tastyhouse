package com.tastyhouse.domain.review.service;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 리뷰 게시중단 요청 워크플로 불변식(도메인 서비스).
 *
 * <p>"점주 요청 → 관리자 심사 → 승인 시 리뷰 숨김"이라는 워크플로의 규칙은 요청자(ceo)와 심사자(admin)가
 * 서로 다른 액터임에도 동일하게 유지되어야 한다. 특히 승인({@link #approve(Long)})은 <b>요청 애그리거트의
 * 상태 전이와 리뷰의 숨김 반영이 한 트랜잭션에서 반드시 함께</b> 일어나야 하는 원자 연산이다 — 둘 중
 * 하나만 반영되면 "승인됐는데 리뷰가 계속 노출되는" 상태가 남는다({@code ShopImageApprovalService}가
 * 이미지 교체에 대해 갖는 것과 같은 성질).
 *
 * <p><b>{@link ShopRequestIndexRecorder}를 생성자 필수 의존으로 받는다</b> — 요청처리 현황
 * ({@code SHOP_REQUEST_INDEX})은 파생 읽기모델이고 기록이 누락되면 그 요청이 통합 목록에서 아예 보이지
 * 않는다. 필수 의존으로 두면 새 상태 전이 메서드를 추가할 때 동기화 배선이 필요하다는 사실이 컴파일
 * 단계에서 드러난다.
 *
 * <p>취소({@link #cancel(Long, Long)})는 원본 애그리거트의 상태 전이이며, 취소 후에는 같은 리뷰에
 * 재요청이 가능해진다 — PENDING 중복 차단이 상태 조회에 기반하므로 코드 추가 없이 자동으로 풀린다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다. 도메인 모델은 POJO라 더티 체킹이 없으므로 변경 후 명시적으로
 * {@code save}를 호출한다.
 */
public class ReviewBlindRequestService {

    private final ReviewBlindRequestRepository reviewBlindRequestRepository;
    private final ReviewRepository reviewRepository;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ReviewBlindRequestService(
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ReviewRepository reviewRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.reviewBlindRequestRepository = reviewBlindRequestRepository;
        this.reviewRepository = reviewRepository;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    /**
     * 점주가 리뷰 게시중단을 요청한다.
     *
     * <p>대상 리뷰가 그 가게의 것인지 역조회로 재검증한다 — 경로의 {@code shopId}만 믿으면 남의 가게
     * 리뷰에 게시중단을 걸 수 있는 IDOR이 된다.
     *
     * <p>같은 트랜잭션에서 {@link ShopRequestIndexRecorder#record}를 호출해 요청처리 현황에 기록한다.
     * 누락되면 그 요청이 통합 목록에서 보이지 않는다.
     *
     * @return 생성된 요청 식별자
     */
    public Long request(Long shopId, Long reviewId, Long ceoId, ReviewBlindReason reason, String detailReason) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        loadReviewOfShop(targetReviewId, shopId);
        validateDetailReason(reason, detailReason);

        if (reviewBlindRequestRepository.existsByReviewIdAndStatus(targetReviewId, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_ALREADY_PENDING);
        }

        ReviewBlindRequest saved = reviewBlindRequestRepository.save(
            ReviewBlindRequest.of(targetReviewId, ShopId.of(shopId), CeoId.of(ceoId), reason, detailReason)
        );

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
     * <p>인덱스 동기화를 <b>리뷰 숨김이 끝난 뒤 마지막에</b> 한다. 지금은 전체가 한 트랜잭션이라 순서를
     * 바꿔도 결과가 같지만, 인덱스는 "요청이 어떻게 처리됐는가"를 답하는 기록이므로 승인이 실제로 반영된
     * 뒤에 APPROVED가 되는 순서가 그 의미와 맞는다.
     */
    public void approve(Long requestId) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.approve();
        request = reviewBlindRequestRepository.save(request);

        Review review = reviewRepository.findById(request.getReviewId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        review.hide();
        reviewRepository.save(review);

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), request.getStatus(), null);
    }

    /**
     * 관리자가 요청을 반려한다. 리뷰는 노출 상태를 유지한다.
     */
    public void reject(Long requestId, String rejectReason) {
        ReviewBlindRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        request = reviewBlindRequestRepository.save(request);

        shopRequestIndexRecorder.syncBlindRequestStatus(request.getId(), request.getStatus(), rejectReason);
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
}
