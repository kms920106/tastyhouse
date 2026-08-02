package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;

/**
 * 가게 이미지 변경 승인요청 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopImageChangeRequest}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopImageChangeRequestMapper}가 수행한다.
 */
@Entity
@Table(name = "SHOP_IMAGE_CHANGE_REQUEST")
public class ShopImageChangeRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopImageType imageType;

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "image_file_id", nullable = false)
    private UploadedFileId imageFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopImageChangeRequestJpaEntity() {
    }

    private ShopImageChangeRequestJpaEntity(
        ShopId shopId,
        ShopImageType imageType,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason
    ) {
        this.shopId = shopId;
        this.imageType = imageType;
        this.imageFileId = imageFileId;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopImageChangeRequestMapper#toEntity}에서만 호출한다.
     */
    static ShopImageChangeRequestJpaEntity create(
        ShopId shopId,
        ShopImageType imageType,
        UploadedFileId imageFileId,
        ApprovalStatus status,
        String rejectReason
    ) {
        return new ShopImageChangeRequestJpaEntity(shopId, imageType, imageFileId, status, rejectReason);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변 필드는 건드리지 않는다.
     */
    void applyChanges(ApprovalStatus status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
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
}
