package com.tastyhouse.core.domain.shop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Entity
@Table(name = "SHOP_BUSINESS_HOUR")
public class ShopBusinessHour {

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
}
