package com.tastyhouse.ceoapi.ceo.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 내 시스템 접근권한 이력 목록 조회 조건.
 *
 * <p>{@code actionType}은 도메인 enum 경계 규칙에 따라 HTTP 경계에서 {@code String}으로 받고 Service에서
 * {@code from(String)}으로 승격한다.
 *
 * <p>{@code shopId}에 소유권 검증을 걸지 않는다 — 어차피 토큰의 {@code ceoId}로 함께 필터하므로 남의
 * 가게 id를 넣으면 빈 목록이 될 뿐이고, 그래서 가게 존재 여부가 새지 않는다.
 *
 * <p><b>날짜 필드에 Bean Validation을 걸지 않는다</b> — {@code CeoLoginHistorySearchRequest}와 같은
 * 이유다. 보관 기간·미래일자 판정은 전부 {@code CeoShopAccessHistoryQueryService}가 담당해
 * {@code CEO_SHOP_ACCESS_HISTORY_DATE_OUT_OF_RANGE} 하나로 응답한다.
 */
public record CeoShopAccessHistorySearchRequest(

    @Schema(
        description = "조치 유형. 미지정 시 전체",
        example = "GRANT",
        allowableValues = {"GRANT", "REVOKE"}
    )
    String actionType,

    @Schema(description = "가게 ID. 미지정 시 전체 가게", example = "12")
    Long shopId,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 시작일(yyyy-MM-dd). 미지정 시 종료일 - 1년", example = "2025-08-14")
    LocalDate startDate,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 종료일(yyyy-MM-dd). 미지정 시 오늘", example = "2026-08-14")
    LocalDate endDate
) {
}
