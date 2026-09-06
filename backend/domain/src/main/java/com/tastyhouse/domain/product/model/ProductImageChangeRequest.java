package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 이미지 등록·변경 승인요청 순수 도메인 모델. 기존 {@code ShopImageChangeRequest} 패턴을 그대로 본뜬다.
 *
 * <p>검수 대상은 <b>새 이미지의 내용</b>이다 — 순서 변경·삭제는 배치일 뿐이므로 승인 없이 즉시 반영한다.
 *
 * <p>같은 메뉴에 PENDING이 2건 생기지 않도록 요청 시점에
 * {@code PRODUCT_IMAGE_CHANGE_REQUEST_ALREADY_PENDING}으로 막는다.
 */
public class ProductImageChangeRequest {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private final UploadedFileId imageFileId; // 요청된 새 이미지 파일 ID
    private ApprovalStatus status;
    private String rejectReason; // nullable
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductImageChangeRequest(
        Long id,
        ProductId productId,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.imageFileId = imageFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 새 검수 요청을 만든다. 항상 {@code PENDING}으로 시작한다. */
    public static ProductImageChangeRequest of(ProductId productId, UploadedFileId imageFileId) {
        return new ProductImageChangeRequest(
            null,
            productId,
            imageFileId,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ProductImageChangeRequest reconstitute(
        Long id,
        ProductId productId,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductImageChangeRequest(
            id,
            productId,
            imageFileId,
            status,
            rejectReason,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
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

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public ProductImageChangeRequestId getRequestId() {
        return ProductImageChangeRequestId.of(this.id);
    }

    /** 승인한다. 검수 대기 상태가 아니면 {@code PRODUCT_IMAGE_CHANGE_REQUEST_NOT_PENDING}(400). */
    public void approve() {
        requirePending();
        this.status = ApprovalStatus.APPROVED;
    }

    /** 반려한다. 사유는 필수다. */
    public void reject(String rejectReason) {
        requirePending();
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    /** 점주가 취소한다. 검수 대기중일 때만 가능하다. */
    public void cancel() {
        requirePending();
        this.status = ApprovalStatus.CANCELED;
        this.rejectReason = null;
    }

    private void requirePending() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_NOT_PENDING);
        }
    }
}
