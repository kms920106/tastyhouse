package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;

/**
 * 가게 운영시간 응답.
 *
 * <p>과거에는 admin·ceo가 바이트 동일하다는 이유로 api-common-module이 이 record를 단독 소유했으나,
 * 그 위치는 <b>표현 계약을 공유 웹 어댑터 모듈이 갖는</b> 배치라 application 계층이 그것을 조립하려면
 * api-common에 의존해야 했다. 지금은 앱별 application이 각자 소유한다 — admin·ceo가 같은 필드
 * 구성을 갖는 것은 중복이 아니라 <b>우연히 일치한 앱별 응답 계약</b>이며, 한쪽 화면 요구가 바뀌면
 * 다른 쪽을 건드리지 않고 갈라질 수 있어야 한다.
 *
 * <p>요일 표시명({@code description})처럼 도메인 enum에서 파생되는 값은 {@code ShopBusinessHourResult}를
 * 받은 QueryService의 private 매퍼가 풀어 넘긴다 — 이 record 자체는 domain-free다.
 */
@Schema(description = "가게 운영시간 응답")
public record ShopBusinessHourResponse(
    @Schema(description = "운영시간 ID", example = "1")
    Long id,

    @Schema(description = "요일 유형", example = "WEEKDAY")
    String dayType,

    @Schema(description = "요일 유형 설명", example = "평일")
    String description,

    @Schema(description = "영업 시작 시각", example = "09:00:00")
    LocalTime openTime,

    @Schema(description = "영업 종료 시각", example = "22:00:00")
    LocalTime closeTime,

    @Schema(description = "휴무 여부", example = "false")
    Boolean isClosed,

    @Schema(description = "24시간 영업 여부", example = "false")
    Boolean is24Hours
) {
    public static ShopBusinessHourResponse from(ShopBusinessHourResult result) {
        return new ShopBusinessHourResponse(
            result.id(),
            result.dayType().name(),
            result.dayType().getDescription(),
            result.openTime(),
            result.closeTime(),
            result.closed(),
            result.allDay()
        );
    }
}
