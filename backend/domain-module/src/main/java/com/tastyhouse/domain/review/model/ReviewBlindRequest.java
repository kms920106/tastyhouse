package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 리뷰 게시중단 요청 순수 도메인 모델 — "점주 요청 → 관리자 심사 → 승인 시 리뷰 숨김 → 30일 뒤 재노출
 * 또는 고객 동의 시 삭제" 워크플로.
 *
 * <p>상태는 공용 {@code ApprovalStatus}가 아니라 도메인 특화 {@link ReviewBlindStatus}를 쓴다 — 사유는
 * 그 enum의 Javadoc 참고.
 *
 * <p><b>취소는 인덱스가 아니라 이 애그리거트의 상태 전이다</b>(backend/CLAUDE.md 「요청 취소 규칙」).
 * 원본이 PENDING으로 남으면 {@code existsByReviewIdAndStatus(PENDING)} 중복 차단이 취소 후에도
 * 재요청을 막고, 관리자가 이미 취소된 요청을 승인·반려할 수 있다.
 *
 * <p>전이 가드는 모두 종결 조건을 <b>값 열거가 아니라 부정형</b>({@code != PENDING} / {@code != APPROVED})
 * 으로 쓴다 — 값을 열거하는 가드는 상수가 추가될 때 조용히 구멍이 생기지만(실제 선례:
 * {@code ShopDeliveryAreaAdjustmentRequest#reject}), 이 형태는 값 추가에 자동으로 안전하다.
 */
public class ReviewBlindRequest {

    /** 승인 시각으로부터 이 일수가 지나면 배치가 리뷰를 자동 재노출한다. */
    public static final int BLIND_PERIOD_DAYS = 30;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ReviewId reviewId;
    private final ShopId shopId;
    private final CeoId ceoId;
    private final ReviewBlindReason reason;
    private final String detailReason; // nullable — reason=ETC일 때만 필수(서비스가 검증)
    private ReviewBlindStatus status;
    private String rejectReason; // nullable — REJECTED일 때만
    private LocalDateTime blindUntil; // nullable — APPROVED일 때만 값
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ReviewBlindRequest(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        ReviewBlindReason reason,
        String detailReason,
        ReviewBlindStatus status,
        String rejectReason,
        LocalDateTime blindUntil,
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
        this.blindUntil = blindUntil;
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
            null, reviewId, shopId, ceoId, reason, detailReason, ReviewBlindStatus.PENDING, null, null, null
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
        ReviewBlindStatus status,
        String rejectReason,
        LocalDateTime blindUntil,
        LocalDateTime createdAt
    ) {
        return new ReviewBlindRequest(
            id, reviewId, shopId, ceoId, reason, detailReason, status, rejectReason, blindUntil, createdAt
        );
    }

    /**
     * 관리자가 요청을 승인한다. <b>리뷰 숨김 반영은 이 애그리거트가 하지 않는다</b> — 두 애그리거트를
     * 함께 다루는 원자 연산이라 {@code ReviewBlindRequestService}가 같은 트랜잭션에서 수행한다.
     *
     * <p><b>재노출 기한을 파라미터로 받는 이유</b>: domain-module은 프레임워크-프리라 시계를 주입받을 수
     * 없고, 도메인이 직접 {@code LocalDateTime.now()}를 부르면 단위 테스트에서 만료를 고정할 수 없다.
     * 호출자인 {@code ReviewBlindRequestService}가 {@code now.plusDays(BLIND_PERIOD_DAYS)}를 계산해
     * 넘긴다({@code ReviewOwnerReplyService}가 {@code LocalDate today}를 받는 것과 같은 형태).
     *
     * @param blindUntil 재노출 예정일시(= 승인 시각 + {@link #BLIND_PERIOD_DAYS}일)
     */
    public void approve(LocalDateTime blindUntil) {
        requirePending();
        this.status = ReviewBlindStatus.APPROVED;
        this.blindUntil = blindUntil;
    }

    /**
     * 관리자가 요청을 반려한다. 리뷰는 노출 상태를 유지한다.
     */
    public void reject(String reason) {
        requirePending();
        this.status = ReviewBlindStatus.REJECTED;
        this.rejectReason = reason;
    }

    /**
     * 점주가 접수 대기 중인 요청을 스스로 철회한다. 취소는 사유 없는 종결이므로 {@code rejectReason}을
     * 비운다 — 반려 사유가 남아 있으면 취소된 요청이 반려된 것처럼 보인다.
     */
    public void cancel() {
        requirePending();
        this.status = ReviewBlindStatus.CANCELED;
        this.rejectReason = null;
    }

    /**
     * 게시중단 기한이 지나 배치가 리뷰를 재노출한다. <b>리뷰의 노출 복원은 이 애그리거트가 하지 않는다</b> —
     * 승인과 마찬가지로 두 애그리거트에 걸친 연산이라 호출부가 같은 트랜잭션에서 함께 수행한다.
     *
     * <p>{@code blindUntil}을 비우는 이유는 그 값이 "언제 재노출될 예정인가"를 뜻하기 때문이다. 재노출이
     * 끝난 뒤에도 남겨 두면 이미 지난 예정일이 화면에 표시된다.
     */
    public void expire() {
        requireApproved();
        this.status = ReviewBlindStatus.EXPIRED;
        this.blindUntil = null;
    }

    /**
     * 고객이 리뷰 삭제에 동의해 요청을 종결한다. <b>리뷰 삭제 자체는 이 애그리거트가 하지 않는다</b> —
     * 사진·태그 정리가 함께 필요하므로 {@code ReviewBlindRequestService}가 리뷰 생애주기 서비스로 위임한다.
     */
    public void deleteByConsent() {
        requireApproved();
        this.status = ReviewBlindStatus.DELETED;
        this.blindUntil = null;
    }

    private void requirePending() {
        if (this.status != ReviewBlindStatus.PENDING) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_PENDING);
        }
    }

    /**
     * 게시중단(APPROVED) 상태에서만 가능한 전이의 가드.
     *
     * <p>값을 열거하지 않고 {@code != APPROVED}로 쓴다 — 상수가 추가돼도 자동으로 안전하다.
     */
    private void requireApproved() {
        if (this.status != ReviewBlindStatus.APPROVED) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED);
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

    public ReviewBlindStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public LocalDateTime getBlindUntil() {
        return this.blindUntil;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
