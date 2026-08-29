package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 요청처리 현황 목록 조회 조건.
 *
 * <p>{@code requestType}/{@code status}는 도메인 enum 경계 규칙에 따라 HTTP 경계에서 {@code String}으로
 * 받고 Service에서 {@code from(String)}으로 승격한다. String 파라미터는 Swagger가 enum 스키마를 자동
 * 생성하지 못하므로 후보값을 {@code allowableValues}로 수동 명시한다.
 *
 * <p>날짜 두 필드에 Bean Validation 제약을 두지 않는다 — 기간의 상·하한 관계(`startDate <= endDate`)가
 * 하나의 규칙이라 서비스가 통째로 판정해 {@code SHOP_REQUEST_DATE_RANGE_INVALID} 하나로 응답한다. 변경이력에서
 * 상한만 {@code @PastOrPresent}로 잡았을 때 컨트롤러 진입 전에 걸려 {@code errorCode} 없는 범용 400이
 * 내려가고, 같은 규칙 위반인데 응답 계약이 상·하한에서 갈렸던 선례가 있다.
 *
 * <p>변경이력과 달리 <b>조회 기간 상한이 없다</b>(근거는 {@code ShopRequestQueryService} Javadoc).
 */
public record ShopRequestSearchRequest(

    @Schema(
        description = "요청 유형. 미지정 시 전체",
        example = "DELIVERY_AREA_ADJUSTMENT",
        allowableValues = {"TRADEMARK_CHANGE", "THUMBNAIL_CHANGE", "DELIVERY_AREA_ADJUSTMENT"}
    )
    String requestType,

    @Schema(
        description = "처리 상태. 미지정 시 전체",
        example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED"}
    )
    String status,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 시작일(yyyy-MM-dd). 미지정 시 하한 없음", example = "2026-07-01")
    LocalDate startDate,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 종료일(yyyy-MM-dd, 당일 포함). 미지정 시 상한 없음", example = "2026-08-12")
    LocalDate endDate
) {
}
