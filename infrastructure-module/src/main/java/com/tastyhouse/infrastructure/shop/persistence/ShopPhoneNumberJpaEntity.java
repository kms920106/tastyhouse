package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 전화번호(다건) JPA 영속 모델. 순수 도메인 모델 {@code ShopPhoneNumber}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_PHONE_NUMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopPhoneNumberJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber; // 전화번호

    @Column(name = "is_primary", nullable = false)
    private boolean primary; // 대표 여부

    @Column(name = "is_virtual", nullable = false)
    private boolean virtual; // 가상번호 여부

    private ShopPhoneNumberJpaEntity(Long shopId, String phoneNumber, boolean primary, boolean virtual) {
        this.shopId = shopId;
        this.phoneNumber = phoneNumber;
        this.primary = primary;
        this.virtual = virtual;
    }

    static ShopPhoneNumberJpaEntity create(Long shopId, String phoneNumber, boolean primary, boolean virtual) {
        return new ShopPhoneNumberJpaEntity(shopId, phoneNumber, primary, virtual);
    }

    void applyChanges(boolean primary) {
        this.primary = primary;
    }
}
