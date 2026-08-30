package com.tastyhouse.webapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "운영시간 정보")
public record ShopBusinessHourItem(
    @Schema(description = "요일 타입", example = "WEEKDAY")
    String dayType,

    @Schema(description = "요일 타입 설명", example = "평일")
    String dayTypeDescription,

    @Schema(description = "오픈 시간", example = "11:00")
    String openTime,

    @Schema(description = "마감 시간", example = "22:00")
    String closeTime,

    @Schema(description = "휴무 여부", example = "false")
    boolean closed,

    @Schema(description = "24시간 영업 여부", example = "false")
    boolean is24Hours
) {
    public static ShopBusinessHourItem from(
        String dayType,
        String dayTypeDescription,
        String openTime,
        String closeTime,
        boolean closed,
        boolean is24Hours
    ) {
        return new ShopBusinessHourItem(
            dayType,
            dayTypeDescription,
            openTime,
            closeTime,
            closed,
            is24Hours
        );
    }
}
