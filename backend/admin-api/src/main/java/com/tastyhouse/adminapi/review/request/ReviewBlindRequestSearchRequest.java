package com.tastyhouse.adminapi.review.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 리뷰 게시중단 요청 심사 목록 조회 조건.
 *
 * <p>{@code status}/{@code reason}은 도메인 enum 경계 규칙에 따라 HTTP 경계에서 {@code String}으로 받고
 * Service에서 승격한다. String 파라미터는 Swagger가 enum 스키마를 자동 생성하지 못하므로 후보값을
 * {@code allowableValues}로 수동 명시한다.
 */
@Schema(description = "리뷰 게시중단 요청 심사 목록 조회 조건")
public record ReviewBlindRequestSearchRequest(
    @Schema(description = "상점 ID", example = "1")
    Long shopId,

    @Schema(
        description = "처리 상태. 미지정 시 전체",
        example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"}
    )
    String status,

    @Schema(
        description = "게시중단 요청 사유. 미지정 시 전체",
        example = "ADVERTISEMENT",
        allowableValues = {"ADVERTISEMENT", "PROFANITY", "IRRELEVANT", "PRIVACY", "ETC"}
    )
    String reason,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 시작일(yyyy-MM-dd). 미지정 시 하한 없음", example = "2026-07-01")
    LocalDate startDate,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회 종료일(yyyy-MM-dd, 당일 포함). 미지정 시 상한 없음", example = "2026-08-12")
    LocalDate endDate
) {
}
