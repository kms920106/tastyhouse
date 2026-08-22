package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 사장님 추천(대표 메뉴) 지정 요청 순수 도메인 모델.
 *
 * <p>{@code shopId}를 요청 자체가 들고 있는 이유는 <b>개수 제한이 가게 단위 불변식</b>이기 때문이다 —
 * "이 가게에 대기 중인 요청이 몇 건인가"를 세려면 메뉴를 거쳐 가게로 역조회하지 않고 요청만으로
 * 답할 수 있어야 한다. 관리자 검수 목록에 가게를 표시하는 데도 같은 값이 쓰인다.
 *
 * <p>승인 결과는 {@code Product.representative}에 반영된다. 요청 이력이 여러 건 쌓여도
 * "현재 이 메뉴가 대표 메뉴인가"의 진실원은 {@code Product} 하나다 — 그래서 대표 메뉴 목록
 * 테이블을 따로 만들지 않고 기존 컬럼을 켜는 방식을 쓴다.
 *
 * <p><b>해제는 이 애그리거트를 거치지 않는다.</b> 지정만 승인 대상이고 해제는 즉시 반영되므로
 * ({@code ProductRepresentativeApprovalService#clearRepresentative}) 해제 이력 행이 생기지 않는다.
 */
public class ProductRepresentativeRequest {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private final ShopId shopId; // 개수 제한 검증·검수 목록의 가게 표시용
    private ApprovalStatus status;
    private String rejectReason; // nullable
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductRepresentativeRequest(
        Long id,
        ProductId productId,
        ShopId shopId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.shopId = shopId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 새 검수 요청을 만든다. 항상 {@code PENDING}으로 시작한다. */
    public static ProductRepresentativeRequest of(ProductId productId, ShopId shopId) {
        return new ProductRepresentativeRequest(
            null,
            productId,
            shopId,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ProductRepresentativeRequest reconstitute(
        Long id,
        ProductId productId,
        ShopId shopId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductRepresentativeRequest(
            id,
            productId,
            shopId,
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

    public ShopId getShopId() {
        return this.shopId;
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

    public ProductRepresentativeRequestId getRequestId() {
        return ProductRepresentativeRequestId.of(this.id);
    }

    /** 승인한다. 검수 대기 상태가 아니면 {@code PRODUCT_REPRESENTATIVE_REQUEST_NOT_PENDING}(400). */
    public void approve() {
        requirePending();
        this.status = ApprovalStatus.APPROVED;
    }

    /** 반려한다. 사유는 필수다 — 왜 대표 메뉴로 부적합했는지 알아야 다시 신청할 수 있다. */
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
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_REQUEST_NOT_PENDING);
        }
    }
}
