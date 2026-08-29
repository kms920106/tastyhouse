package com.tastyhouse.ceoapi.ceo.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 내 로그인 이력 목록 조회 조건.
 *
 * <p>필터가 적어도 {@code @RequestParam} 나열이 아니라 Request record로 감싼다(조회 파라미터 수신 규칙).
 *
 * <p>{@code result}는 도메인 enum 경계 규칙에 따라 HTTP 경계에서 {@code String}으로 받고 Service에서
 * {@code from(String)}으로 승격한다. String 파라미터는 Swagger가 enum 스키마를 자동 생성하지 못하므로
 * 후보값을 {@code allowableValues}로 수동 명시한다.
 *
 * <p><b>날짜 필드에 Bean Validation을 걸지 않는다</b> — {@code @PastOrPresent}는 컨트롤러 진입 전에 걸려
 * {@code errorCode} 없는 범용 400을 내리므로, 같은 규칙 위반인데 입력값에 따라 응답 계약이 갈린다.
 * "조회 가능 기간은 최근 90일"은 상한(미래 금지)과 하한(90일 초과 금지)이 하나의 규칙이므로
 * {@code CeoLoginHistoryQueryService}가 통째로 판정해 {@code CEO_LOGIN_HISTORY_DATE_OUT_OF_RANGE}
 * 하나로 응답한다.
 */
public record CeoLoginHistorySearchRequest(

    @Schema(
        description = "로그인 결과. 미지정 시 전체",
        example = "FAILURE",
        allowableValues = {"SUCCESS", "FAILURE"}
    )
    String result,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 시작일(yyyy-MM-dd). 미지정 시 종료일 - 29일", example = "2026-07-16")
    LocalDate startDate,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 종료일(yyyy-MM-dd). 미지정 시 오늘", example = "2026-08-14")
    LocalDate endDate
) {
}
