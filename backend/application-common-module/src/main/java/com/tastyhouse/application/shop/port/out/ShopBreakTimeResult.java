package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

import com.tastyhouse.domain.shared.model.DayType;

/**
 * 가게 휴게시간 한 건(회원 가게정보·점주 설정·관리 화면 공용).
 *
 * <p>{@link ShopBusinessHourResult}와 같은 이유로 write 포트의 목록 조회와 공존한다 — 도메인 서비스는
 * 영업 상태 판정에 도메인 모델을 로드하고, 화면 조립은 이 투영을 쓴다.
 */
public record ShopBreakTimeResult(
    Long id,
    DayType dayType,
    LocalTime startTime,
    LocalTime endTime
) {
}
