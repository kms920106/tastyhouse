package com.tastyhouse.webapi.member.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.member.port.out.MemberDeliveryAddressItemResult;

@Schema(description = "배달 주소 목록 항목")
public record MemberDeliveryAddressItemResponse(
    @Schema(description = "배달 주소 ID(PK)", example = "12")
    Long id,

    @Schema(description = "주소 별칭(집/회사 등)", example = "집")
    String alias,

    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼1동 678-9")
    String lotAddress,

    @Schema(description = "상세 주소", example = "101동 1001호")
    String detailAddress,

    @Schema(description = "행정동 ID. 주소 매칭에 실패하면 null입니다.", example = "1168064000")
    Long adminDongId,

    @Schema(description = "행정동 전체 이름. 행정동 매칭에 실패하면 null입니다.", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "위도", example = "37.501234")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.039876")
    BigDecimal longitude,

    @Schema(description = "기본 배송지 여부", example = "true")
    boolean defaultAddress
) {
    public static MemberDeliveryAddressItemResponse from(MemberDeliveryAddressItemResult result) {
        return new MemberDeliveryAddressItemResponse(
            result.id(),
            result.alias(),
            result.roadAddress(),
            result.lotAddress(),
            result.detailAddress(),
            result.adminDongId(),
            result.regionName(),
            result.latitude(),
            result.longitude(),
            result.defaultAddress()
        );
    }
}
