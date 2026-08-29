package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

import com.tastyhouse.domain.shared.model.DayType;

/**
 * 가게 영업시간 한 건(회원 가게정보·점주 설정·관리 화면 공용).
 *
 * <p>같은 데이터를 도메인 서비스도 읽지만(휴게시간 범위 검증·영업 상태 판정), 그쪽은 도메인 모델
 * {@code ShopBusinessHour}를 write 포트로 로드한다 — 목적(불변식 vs 표현)과 반환 타입이 다르므로
 * 중복이 아니다(공통 지침의 "양쪽에 같은 데이터를 읽는 메서드 허용").
 *
 * <p>요일 표시명은 {@link DayType}이 소유하므로 enum 자체만 투영하고 소비 측이 꺼낸다.
 */
public record ShopBusinessHourResult(
    Long id,
    DayType dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean closed,
    Boolean allDay
) {
}
