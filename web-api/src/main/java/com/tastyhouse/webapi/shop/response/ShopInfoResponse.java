package com.tastyhouse.webapi.shop.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 기본 정보 응답")
public record ShopInfoResponse(
    @Schema(description = "휴무일 목록")
    List<ShopClosedDayItem> closedDays,

    @Schema(description = "운영시간 목록")
    List<ShopBusinessHourItem> businessHours,

    @Schema(description = "브레이크타임 목록")
    List<ShopBreakTimeItem> breakTimes,

    @Schema(description = "편의시설 목록")
    List<ShopAmenityItem> amenities,

    @Schema(description = "사장님 한마디", example = "사장님의 한마디는 환영의 노래입니다...")
    String ownerMessage,

    @Schema(description = "사장님 한마디 생성일시", example = "2024-01-01T12:00:00")
    LocalDateTime ownerMessageCreatedAt
) {
    public static ShopInfoResponse from(
        List<ShopClosedDayItem> closedDays,
        List<ShopBusinessHourItem> businessHours,
        List<ShopBreakTimeItem> breakTimes,
        List<ShopAmenityItem> amenities,
        String ownerMessage,
        LocalDateTime ownerMessageCreatedAt
    ) {
        return new ShopInfoResponse(
            closedDays,
            businessHours,
            breakTimes,
            amenities,
            ownerMessage,
            ownerMessageCreatedAt
        );
    }
}
