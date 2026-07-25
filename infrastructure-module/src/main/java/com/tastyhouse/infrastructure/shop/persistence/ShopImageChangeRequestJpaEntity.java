package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.shared.model.ApprovalStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 이미지 변경 승인요청 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopImageChangeRequest}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopImageChangeRequestMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "SHOP_IMAGE_CHANGE_REQUEST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopImageChangeRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopImageType imageType;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    private ShopImageChangeRequestJpaEntity(
        Long shopId,
        ShopImageType imageType,
        Long imageFileId,
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
        Long shopId,
        ShopImageType imageType,
        Long imageFileId,
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
}
