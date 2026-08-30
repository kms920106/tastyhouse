package com.tastyhouse.ceoapplication.shop.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 저장된 배달지역 도형 조회 결과.
 *
 * <p><b>도형 미설정은 404가 아니라 {@code exists: false}인 200이다.</b> 도형을 그리지 않고 행정동만
 * 직접 등록한 가게가 정상적으로 존재하므로, 미설정은 오류가 아니라 상태다. 404로 응답하면 화면이 정상
 * 상태를 에러로 처리하게 된다.
 *
 * <p>{@code centerMovedMeters}가 {@code 0}보다 크면 저장 이후 가게 주소가 이전된 것이다 — 7km 상한의
 * 기준점이 달라졌으므로 화면이 재설정을 안내해야 한다.
 */
@Schema(description = "배달지역 도형 조회 결과")
public record ShopDeliveryAreaPolygonResponse(
    @Schema(description = "도형이 설정돼 있는지", example = "true")
    boolean exists,

    @Schema(description = "저장된 도형(링 배열). 미설정 시 null")
    List<List<GeoPointResponse>> rings,

    @Schema(description = "저장 시점 가게 위도 스냅샷. 미설정 시 null", example = "37.500000")
    BigDecimal centerLatitude,

    @Schema(description = "저장 시점 가게 경도 스냅샷. 미설정 시 null", example = "127.036000")
    BigDecimal centerLongitude,

    @Schema(description = "현재 가게 위도", example = "37.500000")
    BigDecimal shopLatitude,

    @Schema(description = "현재 가게 경도", example = "127.036000")
    BigDecimal shopLongitude,

    @Schema(description = "저장 시점 기준점이 현재 좌표에서 이동한 거리(m). 0보다 크면 재설정 안내", example = "0")
    int centerMovedMeters,

    @Schema(description = "기준점에서 최원거리 정점까지의 거리(m). 미설정 시 null", example = "3800")
    Integer maxRadiusMeters,

    @Schema(description = "배달지역 최대 반경(m)", example = "7000")
    int maxAllowedRadiusMeters,

    @Schema(description = "가게배달 기본 노출 반경(m, 표시 전용)", example = "4000")
    int defaultExposureRadiusMeters,

    @Schema(description = "링 개수. 미설정 시 null", example = "1")
    Integer ringCount,

    @Schema(description = "총 정점 수. 미설정 시 null", example = "48")
    Integer vertexCount,

    @Schema(description = "이 도형에서 파생된 배달가능지역 행정동 수", example = "23")
    int projectedAdminDongCount,

    @Schema(description = "도형 최종 수정 일시. 미설정 시 null", example = "2026-08-09T12:00:00")
    LocalDateTime updatedAt
) {

    public static ShopDeliveryAreaPolygonResponse from(
        boolean exists,
        List<List<GeoPointResponse>> rings,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        BigDecimal shopLatitude,
        BigDecimal shopLongitude,
        int centerMovedMeters,
        Integer maxRadiusMeters,
        int maxAllowedRadiusMeters,
        int defaultExposureRadiusMeters,
        Integer ringCount,
        Integer vertexCount,
        int projectedAdminDongCount,
        LocalDateTime updatedAt
    ) {
        return new ShopDeliveryAreaPolygonResponse(
            exists,
            rings,
            centerLatitude,
            centerLongitude,
            shopLatitude,
            shopLongitude,
            centerMovedMeters,
            maxRadiusMeters,
            maxAllowedRadiusMeters,
            defaultExposureRadiusMeters,
            ringCount,
            vertexCount,
            projectedAdminDongCount,
            updatedAt
        );
    }
}
