package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopInfoViewResult;

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
    LocalDateTime ownerMessageCreatedAt,

    @Schema(description = "주차 가능 여부(정보 없으면 null)", example = "true")
    Boolean parkingAvailable,

    @Schema(description = "주차 유료 여부(정보 없으면 null)", example = "false")
    Boolean parkingPaid,

    @Schema(description = "발렛 가능 여부(정보 없으면 null)", example = "false")
    Boolean valetAvailable,

    @Schema(description = "발렛 유료 여부(정보 없으면 null)", example = "false")
    Boolean valetPaid,

    @Schema(description = "찾아오는 길 안내", example = "2번 출구에서 도보 5분")
    String directionsGuide,

    @Schema(description = "지도 노출 위도(설정 시 마커·상세 좌표로 우선 사용 가능)", example = "37.5013")
    BigDecimal displayLatitude,

    @Schema(description = "지도 노출 경도(설정 시 마커·상세 좌표로 우선 사용 가능)", example = "127.0396")
    BigDecimal displayLongitude
) {
    public static ShopInfoResponse from(ShopInfoViewResult result) {
        return new ShopInfoResponse(
            result.closedDays().stream().map(ShopClosedDayItem::from).toList(),
            result.businessHours().stream().map(ShopBusinessHourItem::from).toList(),
            result.breakTimes().stream().map(ShopBreakTimeItem::from).toList(),
            result.amenities().stream().map(ShopAmenityItem::from).toList(),
            result.ownerMessage(),
            result.ownerMessageCreatedAt(),
            result.parkingAvailable(),
            result.parkingPaid(),
            result.valetAvailable(),
            result.valetPaid(),
            result.directionsGuide(),
            result.displayLatitude(),
            result.displayLongitude()
        );
    }
}
