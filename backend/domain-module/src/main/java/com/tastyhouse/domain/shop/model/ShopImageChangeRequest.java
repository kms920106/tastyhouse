package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 이미지(상표/대표이미지) 변경 승인요청 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopImageChangeRequestJpaEntity} + {@code ShopImageChangeRequestMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code ShopImageChangeRequestRepository#save}를 호출해야 한다.
 */
public class ShopImageChangeRequest {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final ShopImageType imageType;
    private final UploadedFileId imageFileId; // 요청된 새 이미지 파일 ID
    private ApprovalStatus status;
    private String rejectReason; // nullable
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopImageChangeRequest(
        Long id,
        ShopId shopId,
        ShopImageType imageType,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.imageType = imageType;
        this.imageFileId = imageFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 이미지 변경 요청을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopImageChangeRequest of(ShopId shopId, ShopImageType imageType, UploadedFileId imageFileId) {
        return new ShopImageChangeRequest(
            null, shopId, imageType, imageFileId, ApprovalStatus.PENDING, null, null, null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopImageChangeRequest reconstitute(
        Long id,
        ShopId shopId,
        ShopImageType imageType,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopImageChangeRequest(id, shopId, imageType, imageFileId, status, rejectReason, createdAt, updatedAt);
    }

    public void approve() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING);
        }
        this.status = ApprovalStatus.APPROVED;
    }

    public void reject(String reason) {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING);
        }
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = reason;
    }

    /**
     * 점주가 접수 대기 중인 요청을 스스로 철회한다.
     *
     * <p>인덱스가 아니라 <b>이 애그리거트</b>에 CANCELED를 두는 이유는, 원본이 PENDING으로 남으면
     * {@code existsByShopIdAndImageTypeAndStatus(PENDING)} 중복 차단이 취소 후에도 재요청을 막고 관리자가
     * 이미 취소된 요청을 승인·반려할 수 있기 때문이다.
     *
     * <p>기존 {@code SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING}을 재사용하지 않고 통합 코드를 쓴다 — 취소는
     * 통합 요청처리 화면의 단일 동작이므로 프론트가 유형별 에러코드 2종을 알 필요가 없어야 한다.
     */
    public void cancel() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_NOT_CANCELABLE);
        }
        this.status = ApprovalStatus.CANCELED;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopImageType getImageType() {
        return this.imageType;
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
}
