package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

/** 시간별 배달팁 한 행(표현용). {@code dayType}은 enum 상수명 문자열이다(HTTP 경계 규칙). */
public record ShopDeliveryTipScheduleResult(
    Long id,
    String dayType,
    LocalTime startTime,
    LocalTime endTime,
    int tipAmount
) {
}
