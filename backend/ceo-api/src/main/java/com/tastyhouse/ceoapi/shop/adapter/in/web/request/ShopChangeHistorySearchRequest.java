package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

public record ShopChangeHistorySearchRequest(

    @Schema(
        description = "변경 대분류. 미지정 시 전체",
        example = "DELIVERY",
        allowableValues = {"OPERATION", "DELIVERY", "SHOP_INFO", "IMAGE", "RIDER"}
    )
    String category,

    @Schema(
        description = "변경 중분류. 미지정 시 전체",
        example = "DELIVERY_TIP_SCHEDULE",
        allowableValues = {
            "BUSINESS_HOUR", "BREAK_TIME", "HOLIDAY_CLOSURE", "CLOSED_DAY", "TEMPORARY_CLOSURE",
            "PHONE_NUMBER", "REPRESENTATIVE_PHONE", "SHOP_VISIBILITY", "ORDER_SUSPENSION",
            "DELIVERY_TIP_TIER", "DELIVERY_TIP_DISTANCE", "DELIVERY_TIP_REGION", "DELIVERY_TIP_SCHEDULE",
            "DELIVERY_TIP_HOLIDAY", "DELIVERY_AREA", "DELIVERY_AREA_RADIUS", "DELIVERY_AREA_POLYGON",
            "DELIVERY_AREA_ADJUSTMENT", "MIN_ORDER_AMOUNT", "SCHEDULED_ORDER",
            "INTRODUCTION", "CONVENIENCE_INFO", "AMENITY", "CONTENT_BOARD",
            "TRADEMARK_CHANGE_REQUEST", "THUMBNAIL_CHANGE_REQUEST",
            "RIDER_VISIT_GUIDE", "RIDER_PICKUP_LOCATION"
        }
    )
    String changeType,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "조회할 변경 발생 날짜(yyyy-MM-dd). 미지정 시 오늘. 최근 6개월까지만 조회 가능", example = "2026-08-11")
    LocalDate changedDate
) {
}
