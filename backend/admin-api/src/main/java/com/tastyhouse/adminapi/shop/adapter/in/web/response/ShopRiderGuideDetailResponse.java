package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;

/**
 * 관리자 검수 화면용 라이더 안내 단건 조회 응답. 문구·픽업 위치와 함께 최근 변경 이력을 내려준다.
 */
@Schema(description = "라이더 안내 상세 조회 응답")
public record ShopRiderGuideDetailResponse(
    @Schema(description = "가게 ID", example = "5")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는 분식")
    String shopName,

    @Schema(description = "가게 실주소(도로명)", example = "서울시 강남구 테헤란로 1")
    String shopRoadAddress,

    @Schema(description = "라이더 가게방문 안내 문구 (미등록 시 null)",
        example = "대로변에서 분홍색 건물 1층 OO 안경 옆 가게입니다.")
    String visitGuide,

    @Schema(description = "라이더 픽업 위치 (미설정 시 null — 가게 실주소로 폴백)")
    ShopRiderPickupLocationResponse pickupLocation,

    @Schema(description = "변경 이력 (최신순, 최대 20건)")
    List<ShopRiderGuideHistoryResponse> histories
) {

    /**
     * 중첩 응답(픽업 위치·변경 이력)은 여기서 조립한다. 픽업 위치는 미설정이면 null이 되며,
     * 이력은 별도 조회 결과라 인자로 받는다.
     */
    public static ShopRiderGuideDetailResponse from(
        ShopRiderGuideResult result,
        List<ShopRiderGuideHistoryResult> histories
    ) {
        return new ShopRiderGuideDetailResponse(
            result.shopId(),
            result.shopName(),
            result.shopRoadAddress(),
            result.visitGuide(),
            ShopRiderPickupLocationResponse.from(result),
            histories.stream().map(ShopRiderGuideHistoryResponse::from).toList()
        );
    }
}
