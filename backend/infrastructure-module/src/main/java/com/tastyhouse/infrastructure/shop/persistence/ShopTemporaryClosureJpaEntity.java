package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점 임시 휴무 JPA 영속 모델. 순수 도메인 모델 {@code ShopTemporaryClosure}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_TEMPORARY_CLOSURE")
public class ShopTemporaryClosureJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // 임시 휴무 시작일

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // 임시 휴무 종료일

    protected ShopTemporaryClosureJpaEntity() {
    }

    private ShopTemporaryClosureJpaEntity(ShopId shopId, LocalDate startDate, LocalDate endDate) {
        this.shopId = shopId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopTemporaryClosureMapper#toEntity}에서만 호출한다.
     */
    static ShopTemporaryClosureJpaEntity create(ShopId shopId, LocalDate startDate, LocalDate endDate) {
        return new ShopTemporaryClosureJpaEntity(shopId, startDate, endDate);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }
}
