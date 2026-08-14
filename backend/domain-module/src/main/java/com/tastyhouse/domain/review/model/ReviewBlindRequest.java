package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 리뷰 게시중단 요청 순수 도메인 모델 — "점주 요청 → 관리자 심사 → 승인 시 리뷰 숨김" 워크플로.
 *
 * <p>기존에는 관리자가 {@code REVIEW.is_hidden}을 직접 토글할 뿐 요청 워크플로가 없었다. 공용
 * {@link ApprovalStatus}와 {@code ShopRequestIndex} 패턴을 재사용해
 * {@code ShopImageChangeRequest}와 같은 형태로 맞춘다.
 *
 * <p><b>취소는 인덱스가 아니라 이 애그리거트의 상태 전이다</b>(backend/CLAUDE.md 「요청 취소 규칙」).
 * 원본이 PENDING으로 남으면 {@code existsByReviewIdAndStatus(PENDING)} 중복 차단이 취소 후에도
 * 재요청을 막고, 관리자가 이미 취소된 요청을 승인·반려할 수 있다.
 *
 * <p>세 전이 모두 종결 조건을 {@code != PENDING}으로 쓴다 — 값을 열거하는 가드는 상수가 추가될 때
 * 조용히 구멍이 생기지만(실제 선례: {@code ShopDeliveryAreaAdjustmentRequest#reject}), 이 형태는
 * 값 추가에 자동으로 안전하다.
 */
public class ReviewBlindRequest {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ReviewId reviewId;
    private final ShopId shopId;
    private final CeoId ceoId;
    private final ReviewBlindReason reason;
    private final String detailReason; // nullable — reason=ETC일 때만 필수(서비스가 검증)
    private ApprovalStatus status;
    private String rejectReason; // nullable — REJECTED일 때만
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ReviewBlindRequest(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        ReviewBlindReason reason,
        String detailReason,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.reason = reason;
        this.detailReason = detailReason;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
    }

    /**
     * 신규 게시중단 요청을 접수한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ReviewBlindRequest of(
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        ReviewBlindReason reason,
        String detailReason
    ) {
        return new ReviewBlindRequest(
            null, reviewId, shopId, ceoId, reason, detailReason, ApprovalStatus.PENDING, null, null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ReviewBlindRequest reconstitute(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        ReviewBlindReason reason,
        String detailReason,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt
    ) {
        return new ReviewBlindRequest(
            id, reviewId, shopId, ceoId, reason, detailReason, status, rejectReason, createdAt
        );
    }

    /**
     * 관리자가 요청을 승인한다. <b>리뷰 숨김 반영은 이 애그리거트가 하지 않는다</b> — 두 애그리거트를
     * 함께 다루는 원자 연산이라 {@code ReviewBlindRequestService}가 같은 트랜잭션에서 수행한다.
     */
    public void approve() {
        requirePending();
        this.status = ApprovalStatus.APPROVED;
    }

    /**
     * 관리자가 요청을 반려한다. 리뷰는 노출 상태를 유지한다.
     */
    public void reject(String reason) {
        requirePending();
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = reason;
    }

    /**
     * 점주가 접수 대기 중인 요청을 스스로 철회한다. 취소는 사유 없는 종결이므로 {@code rejectReason}을
     * 비운다 — 반려 사유가 남아 있으면 취소된 요청이 반려된 것처럼 보인다.
     */
    public void cancel() {
        requirePending();
        this.status = ApprovalStatus.CANCELED;
        this.rejectReason = null;
    }

    private void requirePending() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_PENDING);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public ReviewBlindReason getReason() {
        return this.reason;
    }

    public String getDetailReason() {
        return this.detailReason;
    }

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
