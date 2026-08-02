package com.tastyhouse.domain.shop.domain.model;

import java.time.DayOfWeek;
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

    /**
     * 이 휴게시간이 주어진 시각을 포함하는지 판정한다.
     *
     * <p>요일 구분({@link DayType})이 오늘에 적용되지 않거나 시작·종료 시각이 없으면 포함하지 않는다.
     * 포함 구간은 {@code [startTime, endTime)} 반열림이며, 종료가 시작보다 이르면 자정을 넘기는 구간으로
     * 본다.
     *
     * <p>이 판정은 원래 {@code ShopOperatingStatusCalculator}가 {@code getStartTime()}/{@code getEndTime()}
     * /{@code getDayType()}을 꺼내 수행했다 — 휴게시간이 스스로 답해야 할 질문이라 모델로 이식했다.
     */
    public boolean covers(LocalTime time, DayOfWeek dayOfWeek, boolean publicHoliday) {
        if (startTime == null || endTime == null) {
            return false;
        }
        if (!dayType.appliesTo(dayOfWeek, publicHoliday)) {
            return false;
        }
        if (endTime.isBefore(startTime)) {
            return !time.isBefore(startTime) || time.isBefore(endTime);
        }
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }
}
