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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.shop.domain.model.DayType;

/**
 * 상점 영업시간 JPA 영속 모델. 순수 도메인 모델 {@code ShopBusinessHour}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_BUSINESS_HOUR")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopBusinessHourJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType; // 요일 유형 (WEEKDAY, SATURDAY, SUNDAY, HOLIDAY 등)

    @Column(name = "open_time")
    private LocalTime openTime; // 영업 시작 시각

    @Column(name = "close_time")
    private LocalTime closeTime; // 영업 종료 시각

    @Column(name = "is_closed")
    private Boolean isClosed; // 휴무 여부 (true: 휴무)

    @Column(name = "is_open_24_hours")
    private Boolean is24Hours; // 24시간 영업 여부

    private ShopBusinessHourJpaEntity(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        this.shopId = shopId;
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    static ShopBusinessHourJpaEntity create(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        return new ShopBusinessHourJpaEntity(shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    void applyChanges(DayType dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }
}
