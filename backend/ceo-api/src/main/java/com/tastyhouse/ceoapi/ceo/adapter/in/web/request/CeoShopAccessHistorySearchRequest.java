package com.tastyhouse.ceoapi.ceo.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

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
