package com.tastyhouse.webapi.member.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 배달 주소 목록 항목 응답.
 *
 * <p>회원 도메인 응답 record는 최상위 응답에 {@code Response} 접미를 쓰므로({@code MyProfileResponse}
 * 등) 목록 항목도 {@code ItemResponse}로 둔다 — web-api {@code shop/response/}의 {@code Item} 접미는
 * 다른 응답 안에 중첩되는 <b>요소</b> record의 관행이고, 이 record는 응답 본문의 최상위 요소다.
 */
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

    public static MemberDeliveryAddressItemResponse from(
        Long id,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        Long adminDongId,
        String regionName,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        return new MemberDeliveryAddressItemResponse(
            id,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            regionName,
            latitude,
            longitude,
            defaultAddress
        );
    }
}
