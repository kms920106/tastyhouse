package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 매장 가격 인증 요청 순수 도메인 모델 — 가격표 이미지를 근거로 매장가를 승인받는 검수 애그리거트.
 *
 * <p><b>요청 시점의 매장가는 이 애그리거트가 아니라 항목({@link StorePriceVerificationItem})이
 * 보관한다.</b> 승인이 나중에 이뤄지므로 그 사이 점주가 가격을 바꿔도 <b>검수자가 본 가격 그대로</b>
 * 반영돼야 하기 때문이다 — 승인 시점에 현재 가격을 다시 읽으면 검수하지 않은 값이 승인된다.
 *
 * <p>이 요청은 기존 점주 요청 통합 인덱스({@code SHOP_REQUEST_INDEX})에 올라탄다 —
 * {@code attachment_file_id}(가격표 이미지)·{@code IN_PROGRESS}·{@code reject_reason}·반려 사유 문의
 * 스레드가 이미 갖춰져 있어 병렬 큐를 새로 만들 이유가 없다. 배선은
 * {@code ShopRequestIndexRecorder}가 담당한다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code StorePriceVerificationJpaEntity} + 매퍼가 담당하며, 더티 체킹이 없으므로 상태 전이 후
 * 저장은 호출부가 명시적으로 {@code save}를 호출해야 한다.
 */
public class StorePriceVerification {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final UploadedFileId priceListFileId; // 매장 가격표 이미지
    private StorePriceVerificationStatus status;
    private String rejectReason; // nullable — REJECTED일 때만
    private final Long requestedByCeoId; // nullable — admin 대행 접수 경로가 생기면 null
    private LocalDateTime processedAt; // nullable — 접수 직후 null
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private StorePriceVerification(
        Long id,
        ShopId shopId,
        UploadedFileId priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.priceListFileId = priceListFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 새 인증 요청을 만든다. 접수 직후이므로 항상 {@code PENDING}이고 처리 시각·반려 사유는 없다. */
    public static StorePriceVerification of(
        ShopId shopId,
        UploadedFileId priceListFileId,
        Long requestedByCeoId
    ) {
        return new StorePriceVerification(
            null,
            shopId,
            priceListFileId,
            StorePriceVerificationStatus.PENDING,
            null,
            requestedByCeoId,
            null,
            null,
            null
        );
    }

    /** DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다. */
    public static StorePriceVerification reconstitute(
        Long id,
        ShopId shopId,
        UploadedFileId priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new StorePriceVerification(
            id,
            shopId,
            priceListFileId,
            status,
            rejectReason,
            requestedByCeoId,
            processedAt,
            createdAt,
            updatedAt
        );
    }

    /**
     * 검수에 착수한다({@code PENDING} → {@code IN_PROGRESS}).
     *
     * <p>대기 상태에서만 가능하다 — 이미 종결된 요청을 다시 검수 중으로 되돌리지 않는다.
     */
    public void startReview(LocalDateTime now) {
        if (this.status != StorePriceVerificationStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_PENDING);
        }
        this.status = StorePriceVerificationStatus.IN_PROGRESS;
        this.processedAt = now;
    }

    /**
     * 승인한다. 실제 가격 반영은 이 애그리거트가 아니라
     * {@code StorePriceVerificationService}가 항목을 순회해 수행한다 — 가격 행은 다른 애그리거트다.
     */
    public void approve(LocalDateTime now) {
        requireOpen();
        this.status = StorePriceVerificationStatus.APPROVED;
        this.rejectReason = null;
        this.processedAt = now;
    }

    /** 반려한다. 사유는 필수다 — 무엇이 부적합했는지 알아야 다시 요청할 수 있다. */
    public void reject(String rejectReason, LocalDateTime now) {
        requireOpen();
        this.status = StorePriceVerificationStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.processedAt = now;
    }

    /** 점주가 검수 대기·진행 중인 요청을 취소한다. 취소는 사유 없는 종결이다. */
    public void cancel(LocalDateTime now) {
        requireOpen();
        this.status = StorePriceVerificationStatus.CANCELED;
        this.rejectReason = null;
        this.processedAt = now;
    }

    /**
     * 아직 처리 중인 요청만 상태를 전이할 수 있다 — 종결된 요청의 재전이는 검수 결과를 덮어쓴다.
     */
    private void requireOpen() {
        if (!this.status.isOpen()) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_PENDING);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public UploadedFileId getPriceListFileId() {
        return this.priceListFileId;
    }

    public StorePriceVerificationStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public Long getRequestedByCeoId() {
        return this.requestedByCeoId;
    }

    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public StorePriceVerificationId getVerificationId() {
        return StorePriceVerificationId.of(this.id);
    }
}
