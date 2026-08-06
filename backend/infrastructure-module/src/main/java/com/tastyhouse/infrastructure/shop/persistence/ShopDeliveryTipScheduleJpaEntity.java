package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 시간별 추가 배달팁 JPA 영속 모델.
 *
 * <p>{@code day_type}은 {@code DayType}을 재사용하되 {@code HOLIDAY}는 저장되지 않는다 — 공휴일은 전용
 * 애그리거트가 담당하며, 그 금지는 도메인 모델 {@code ShopDeliveryTipSchedule#of}가 강제한다.
 *
 * <p>시간별 컬렉션도 replace-all 교체라 {@code applyChanges}가 없다.
 */
@Entity
@Table(name = "SHOP_DELIVERY_TIP_SCHEDULE")
public class ShopDeliveryTipScheduleJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType; // 요일 구분 (HOLIDAY 제외)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // 시작 시각

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // 종료 시각 (시작보다 이르면 자정 넘김)

    @Column(name = "tip_amount", nullable = false)
    private int tipAmount; // 추가 배달팁 (0~10,000)

    protected ShopDeliveryTipScheduleJpaEntity() {
    }

    private ShopDeliveryTipScheduleJpaEntity(
        Long shopId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        int tipAmount
    ) {
        this.shopId = shopId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tipAmount = tipAmount;
    }

    static ShopDeliveryTipScheduleJpaEntity create(
        Long shopId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        int tipAmount
    ) {
        return new ShopDeliveryTipScheduleJpaEntity(shopId, dayType, startTime, endTime, tipAmount);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public DayType getDayType() {
        return this.dayType;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
