package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonViewResult;
import com.tastyhouse.application.shop.port.out.GeoPointView;

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
    public static ShopDeliveryAreaPolygonResponse from(ShopDeliveryAreaPolygonViewResult result) {
        List<List<GeoPointView>> rings = result.rings();
        return new ShopDeliveryAreaPolygonResponse(
            result.exists(),
            rings == null ? null : rings.stream()
                .map(ring -> ring.stream().map(GeoPointResponse::from).toList())
                .toList(),
            result.centerLatitude(),
            result.centerLongitude(),
            result.shopLatitude(),
            result.shopLongitude(),
            result.centerMovedMeters(),
            result.maxRadiusMeters(),
            result.maxAllowedRadiusMeters(),
            result.defaultExposureRadiusMeters(),
            result.ringCount(),
            result.vertexCount(),
            result.projectedAdminDongCount(),
            result.updatedAt()
        );
    }
}
