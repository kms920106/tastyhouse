package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

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
