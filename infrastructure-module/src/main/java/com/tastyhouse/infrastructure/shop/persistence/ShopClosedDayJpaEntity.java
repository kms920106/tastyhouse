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

import com.tastyhouse.domain.shop.domain.model.ClosedDayType;

/**
 * 상점 정기 휴무 JPA 영속 모델. 순수 도메인 모델 {@code ShopClosedDay}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_CLOSED_DAY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopClosedDayJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_day_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private ClosedDayType closedDayType; // 정기 휴무 유형 (FIRST_MON, SECOND_SUN, EVERY_TUE 등)

    private ShopClosedDayJpaEntity(Long shopId, ClosedDayType closedDayType) {
        this.shopId = shopId;
        this.closedDayType = closedDayType;
    }

    static ShopClosedDayJpaEntity create(Long shopId, ClosedDayType closedDayType) {
        return new ShopClosedDayJpaEntity(shopId, closedDayType);
    }
}
