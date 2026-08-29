package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 가게 변경이력 목록 조회 조건.
 *
 * <p>필터가 적어도 {@code @RequestParam} 나열이 아니라 Request record로 감싼다(조회 파라미터 수신 규칙).
 *
 * <p>{@code category}/{@code changeType}은 도메인 enum 경계 규칙에 따라 HTTP 경계에서 {@code String}으로
 * 받고 Service에서 {@code from(String)}으로 승격한다. String 파라미터는 Swagger가 enum 스키마를 자동
 * 생성하지 못하므로 후보값을 {@code allowableValues}로 수동 명시한다.
 *
 * <p>{@code changedDate}에는 Bean Validation 제약을 두지 않는다 — "조회 가능 기간은 최근 6개월(과거~오늘)"은
 * 상한(미래 금지)과 하한(6개월 초과 금지)이 <b>하나의 규칙</b>이므로 {@code ShopChangeHistoryQueryService}가
 * 통째로 판정해 {@code SHOP_CHANGE_HISTORY_DATE_OUT_OF_RANGE} 하나로 응답한다. 과거에 상한만
 * {@code @PastOrPresent}로 잡았을 때는 미래 날짜가 컨트롤러 진입 전에 걸려 {@code errorCode} 없는 범용 400이
 * 내려갔고, 같은 규칙 위반인데 프론트가 받는 응답 계약이 상·하한에서 갈리는 문제가 있었다.
 */
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
