package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalTime;

import lombok.Getter;

/**
 * 상점 브레이크타임 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopBreakTimeJpaEntity} + {@code ShopBreakTimeMapper}가 담당한다.
 */
@Getter
public class ShopBreakTime {

    private final Long id;
    private final Long shopId;
    private DayType dayType;
    private LocalTime startTime;
    private LocalTime endTime;

    private ShopBreakTime(Long id, Long shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.shopId = shopId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static ShopBreakTime of(Long shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        return new ShopBreakTime(null, shopId, dayType, startTime, endTime);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopBreakTime reconstitute(Long id, Long shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        return new ShopBreakTime(id, shopId, dayType, startTime, endTime);
    }

    public void update(DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
