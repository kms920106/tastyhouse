package com.tastyhouse.ceoapi.shop.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 반경 미리보기 결과.
 *
 * <p>{@code circle}(72각형 근사 원)을 함께 내려주는 이유는, 화면이 서버와 <b>같은 도형</b>을 그리게 하기
 * 위해서다. 클라이언트가 자체 공식으로 원을 그리면 경도 보정({@code 1/cos φ}) 유무에 따라 서버 판정과
 * 눈에 보이는 원이 어긋난다.
 *
 * <p>{@code unresolvedCount}는 좌표·경계를 보유하지 않아 판정하지 못한 동 수다. 조용히 감추지 않고
 * 노출해 시드 데이터 공백을 점주와 운영이 인지할 수 있게 한다.
 */
@Schema(description = "반경 미리보기 결과")
public record ShopDeliveryAreaRadiusPreviewResponse(
    @Schema(description = "가게 현재 위도", example = "37.500000")
    BigDecimal centerLatitude,

    @Schema(description = "가게 현재 경도", example = "127.036000")
    BigDecimal centerLongitude,

    @Schema(description = "요청 반경(m)", example = "4000")
    int radiusMeters,

    @Schema(description = "배달지역 최대 반경(m)", example = "7000")
    int maxAllowedRadiusMeters,

    @Schema(description = "가게배달 기본 노출 반경(m, 표시 전용)", example = "4000")
    int defaultExposureRadiusMeters,

    @Schema(description = "반경 원을 근사한 다각형 정점(클라이언트 렌더링용)")
    List<GeoPointResponse> circle,

    @Schema(description = "반경 안에 드는 행정동 목록")
    List<ShopDeliveryAreaCandidateResponse> adminDongs,

    @Schema(description = "반경 안에 드는 행정동 수", example = "23")
    int adminDongCount,

    @Schema(description = "좌표·경계 미보유로 판정하지 못한 행정동 수", example = "0")
    int unresolvedCount
) {

    public static ShopDeliveryAreaRadiusPreviewResponse from(
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        int radiusMeters,
        int maxAllowedRadiusMeters,
        int defaultExposureRadiusMeters,
        List<GeoPointResponse> circle,
        List<ShopDeliveryAreaCandidateResponse> adminDongs,
        int adminDongCount,
        int unresolvedCount
    ) {
        return new ShopDeliveryAreaRadiusPreviewResponse(
            centerLatitude,
            centerLongitude,
            radiusMeters,
            maxAllowedRadiusMeters,
            defaultExposureRadiusMeters,
            circle,
            adminDongs,
            adminDongCount,
            unresolvedCount
        );
    }
}
