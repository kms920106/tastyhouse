package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 채식 설정 승인요청 순수 도메인 모델.
 *
 * <p>{@code ingredients}(채소 외 포함 재료)가 <b>필수</b>인 이유는 그것이 검수의 유일한 근거이기
 * 때문이다 — 관리자가 재료를 보지 않고는 이 메뉴가 정말 그 채식 단계에 해당하는지 판정할 수 없다.
 *
 * <p>승인 결과는 {@code Product.vegetarianType}에 반영된다. 요청 이력이 여러 건 쌓여도
 * "현재 이 메뉴가 채식인가"의 진실원은 {@code Product} 하나다.
 */
public class ProductVegetarianRequest {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private final VegetarianType vegetarianType;
    private final String ingredients; // 채소 외 포함 재료 (검수 근거)
    private final String description; // nullable, 메뉴 설명 (검수 근거)
    private ApprovalStatus status;
    private String rejectReason; // nullable
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductVegetarianRequest(
        Long id,
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.vegetarianType = vegetarianType;
        this.ingredients = ingredients;
        this.description = description;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 새 검수 요청을 만든다. 항상 {@code PENDING}으로 시작한다. */
    public static ProductVegetarianRequest of(
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description
    ) {
        return new ProductVegetarianRequest(
            null,
            productId,
            vegetarianType,
            ingredients,
            description,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ProductVegetarianRequest reconstitute(
        Long id,
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductVegetarianRequest(
            id,
            productId,
            vegetarianType,
            ingredients,
            description,
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

    public VegetarianType getVegetarianType() {
        return this.vegetarianType;
    }

    public String getIngredients() {
        return this.ingredients;
    }

    public String getDescription() {
        return this.description;
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

    public ProductVegetarianRequestId getRequestId() {
        return ProductVegetarianRequestId.of(this.id);
    }

    /** 승인한다. 검수 대기 상태가 아니면 {@code PRODUCT_VEGETARIAN_REQUEST_NOT_PENDING}(400). */
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
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_NOT_PENDING);
        }
    }
}
