package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRequestAdjustmentDetailResult;

/**
 * 요청처리 현황 상세의 배달지역 조정 신청 부분. {@code requestType}이 조정 신청일 때만 채워진다.
 */
@Schema(description = "배달지역 조정 신청 상세")
public record ShopRequestAdjustmentResponse(

    @Schema(description = "상대 가맹점명", example = "맛있는집 강남점")
    String counterpartShopName,

    @Schema(description = "상대 가맹점 사업자등록번호", example = "1234567890")
    String counterpartBusinessNumber,

    @Schema(description = "가맹본부명", example = "BBQ")
    String franchiseName,

    @Schema(description = "조정 신청 사유", example = "배달 권역이 3개 행정동에서 중첩됩니다.")
    String reason,

    @Schema(description = "정보제공 동의서 URL", example = "https://storage.example.com/2026/08/consent.pdf")
    String consentFileUrl
) {

    public static ShopRequestAdjustmentResponse from(ShopRequestAdjustmentDetailResult result) {
        return new ShopRequestAdjustmentResponse(
            result.counterpartShopName(),
            result.counterpartBusinessNumber(),
            result.franchiseName(),
            result.reason(),
            result.consentFileUrl()
        );
    }
}
