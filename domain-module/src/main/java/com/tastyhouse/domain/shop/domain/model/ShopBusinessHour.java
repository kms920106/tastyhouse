package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalTime;

import lombok.Getter;

/**
 * 상점 영업시간 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopBusinessHourJpaEntity} + {@code ShopBusinessHourMapper}가 담당한다.
 */
@Getter
public class ShopBusinessHour {

    private final Long id;
    private final Long shopId;
    private DayType dayType;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;
    private Boolean is24Hours; // 24시간 영업 여부 (true면 openTime/closeTime 무관)

    private ShopBusinessHour(
        Long id,
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        this.id = id;
        this.shopId = shopId;
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    public static ShopBusinessHour of(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        return new ShopBusinessHour(null, shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopBusinessHour reconstitute(
        Long id,
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        return new ShopBusinessHour(id, shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    public void update(DayType dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }
}
