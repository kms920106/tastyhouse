package com.tastyhouse.ceoapi.ceo.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

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
