package com.tastyhouse.webapi.shop.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가게 기본 정보 응답")
public record ShopInfoResponse(
    @Schema(description = "휴무일 목록")
    List<ClosedDayItem> closedDays,

    @Schema(description = "운영시간 목록")
    List<BusinessHourItem> businessHours,

    @Schema(description = "브레이크타임 목록")
    List<BreakTimeItem> breakTimes,

    @Schema(description = "편의시설 목록")
    List<AmenityItem> amenities,

    @Schema(description = "사장님 한마디", example = "사장님의 한마디는 환영의 노래입니다...")
    String ownerMessage,

    @Schema(description = "사장님 한마디 생성일시", example = "2024-01-01T12:00:00")
    LocalDateTime ownerMessageCreatedAt
) {
    public static ShopInfoResponse from(
        List<ClosedDayItem> closedDays,
        List<BusinessHourItem> businessHours,
        List<BreakTimeItem> breakTimes,
        List<AmenityItem> amenities,
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

    @Schema(description = "운영시간 정보")
    public record BusinessHourItem(
        @Schema(description = "요일 타입", example = "WEEKDAY")
        String dayType,

        @Schema(description = "요일 타입 설명", example = "평일")
        String dayTypeDescription,

        @Schema(description = "오픈 시간", example = "11:00")
        String openTime,

        @Schema(description = "마감 시간", example = "22:00")
        String closeTime,

        @Schema(description = "휴무 여부", example = "false")
        boolean closed
    ) {
        public static BusinessHourItem from(
            String dayType,
            String dayTypeDescription,
            String openTime,
            String closeTime,
            boolean closed
        ) {
            return new BusinessHourItem(
                dayType,
                dayTypeDescription,
                openTime,
                closeTime,
                closed
            );
        }
    }

    @Schema(description = "브레이크타임 정보")
    public record BreakTimeItem(
        @Schema(description = "요일 타입", example = "WEEKDAY")
        String dayType,

        @Schema(description = "요일 타입 설명", example = "평일")
        String dayTypeDescription,

        @Schema(description = "브레이크타임 시작", example = "15:00")
        String startTime,

        @Schema(description = "브레이크타임 종료", example = "17:00")
        String endTime
    ) {
        public static BreakTimeItem from(
            String dayType,
            String dayTypeDescription,
            String startTime,
            String endTime
        ) {
            return new BreakTimeItem(
                dayType,
                dayTypeDescription,
                startTime,
                endTime
            );
        }
    }

    @Schema(description = "휴무일 정보")
    public record ClosedDayItem(
        @Schema(description = "휴무일 타입", example = "EVERY_WEEK_MONDAY")
        String closedDayType,

        @Schema(description = "휴무일 설명", example = "매주 월요일")
        String description
    ) {
        public static ClosedDayItem from(
            String closedDayType,
            String description
        ) {
            return new ClosedDayItem(
                closedDayType,
                description
            );
        }
    }

    @Schema(description = "편의시설 정보")
    public record AmenityItem(
        @Schema(description = "편의시설 코드", example = "PARKING")
        String code,

        @Schema(description = "편의시설 표시명", example = "주차 가능")
        String name,

        @Schema(description = "편의시설 활성 이미지 URL", example = "https://example.com/parking-on.png")
        String activeImageUrl
    ) {
        public static AmenityItem from(
            String code,
            String name,
            String activeImageUrl
        ) {
            return new AmenityItem(
                code,
                name,
                activeImageUrl
            );
        }
    }
}
