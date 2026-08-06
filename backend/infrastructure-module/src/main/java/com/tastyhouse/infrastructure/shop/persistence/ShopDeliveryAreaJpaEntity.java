package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 배달가능지역 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopDeliveryArea}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopDeliveryAreaMapper}가 수행한다.
 *
 * <p>등록·삭제만 있고 상태전이가 없어 {@code applyChanges}를 두지 않는다({@code MemberFollowJpaEntity} 선례).
 */
@Entity
@Table(
    name = "SHOP_DELIVERY_AREA",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_shop_delivery_area",
        columnNames = {"shop_id", "admin_dong_id"}
    ),
    indexes = @Index(name = "idx_shop_delivery_area_shop_id", columnList = "shop_id")
)
public class ShopDeliveryAreaJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "admin_dong_id", nullable = false)
    private Long adminDongId;

    protected ShopDeliveryAreaJpaEntity() {
    }

    private ShopDeliveryAreaJpaEntity(Long shopId, Long adminDongId) {
        this.shopId = shopId;
        this.adminDongId = adminDongId;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopDeliveryAreaMapper#toEntity}에서만 호출한다.
     */
    static ShopDeliveryAreaJpaEntity create(Long shopId, Long adminDongId) {
        return new ShopDeliveryAreaJpaEntity(shopId, adminDongId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getAdminDongId() {
        return this.adminDongId;
    }
}
