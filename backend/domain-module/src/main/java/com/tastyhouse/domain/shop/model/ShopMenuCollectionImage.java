package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

/**
 * 메뉴모음컷 순수 도메인 모델 — 손님이 가게를 열었을 때 <b>가장 먼저, 가장 상단에서</b> 보는 이미지.
 *
 * <p><b>승인 상태를 별도 요청 테이블이 아니라 이 행에 직접 보유한다.</b> 검수 대상이 "이 이미지 자체"이고
 * 승인 전에는 손님 화면에 노출되지 않으므로 요청과 결과물이 1:1이다. {@code ProductImageChangeRequest}처럼
 * 요청과 결과물을 분리하면 승인 시 행을 옮겨 담아야 하는데, 이쪽은 {@code sort}(표시 순서)를 <b>승인
 * 전부터 점주가 관리</b>하므로 행을 옮기는 순간 순서가 흔들린다.
 *
 * <p>그래서 {@code sort}도 {@code status}와 함께 가변이다 — 순서 변경은 승인을 거치지 않고 즉시
 * 반영되는 연산이라({@code ShopMenuCollectionImageService#reorder}) 새 인스턴스로 재구성하는 대신
 * 전이 메서드로 바꾼다.
 */
public class ShopMenuCollectionImage {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final UploadedFileId imageFileId;
    private int sort; // 표시 순서(0-base). 순서 변경은 승인 없이 즉시 반영되므로 가변이다.
    private ApprovalStatus status;
    private String rejectReason; // nullable, REJECTED일 때만 채워진다
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopMenuCollectionImage(
        Long id,
        ShopId shopId,
        UploadedFileId imageFileId,
        int sort,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 메뉴모음컷을 등록한다. 항상 {@code PENDING}으로 시작한다 — 손님이 가장 먼저 보는 이미지라
     * 내용 검수를 거치지 않은 것이 노출되면 안 된다.
     */
    public static ShopMenuCollectionImage of(ShopId shopId, UploadedFileId imageFileId, int sort) {
        return new ShopMenuCollectionImage(
            null,
            shopId,
            imageFileId,
            sort,
            ApprovalStatus.PENDING,
            null,
            null,
            null
        );
    }

    public static ShopMenuCollectionImage reconstitute(
        Long id,
        ShopId shopId,
        UploadedFileId imageFileId,
        int sort,
        ApprovalStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopMenuCollectionImage(
            id,
            shopId,
            imageFileId,
            sort,
            status,
            rejectReason,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public int getSort() {
        return this.sort;
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

    public ShopMenuCollectionImageId getImageId() {
        return ShopMenuCollectionImageId.of(this.id);
    }

    /** 승인한다 — 이 시점부터 손님 화면에 노출된다. */
    public void approve() {
        requirePending();
        this.status = ApprovalStatus.APPROVED;
        this.rejectReason = null;
    }

    /** 반려한다. 사유는 필수다 — 점주가 무엇을 고쳐 다시 올려야 하는지 알아야 한다. */
    public void reject(String rejectReason) {
        requirePending();
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    /**
     * 표시 순서를 바꾼다. <b>상태를 건드리지 않는다</b> — 검수 대상은 이미지의 내용이지 배치가 아니므로
     * 순서를 바꿔도 이미 승인된 것이 다시 대기로 돌아가지 않는다.
     */
    public void changeSort(int sort) {
        this.sort = sort;
    }

    /** 검수 대기 상태가 아니면 {@code SHOP_MENU_COLLECTION_IMAGE_NOT_PENDING}(409). */
    private void requirePending() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_PENDING);
        }
    }
}
