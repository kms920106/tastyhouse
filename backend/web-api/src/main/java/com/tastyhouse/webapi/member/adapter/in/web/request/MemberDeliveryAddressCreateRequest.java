package com.tastyhouse.webapi.member.adapter.in.web.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.member.port.in.MemberDeliveryAddressCreateCommand;

@Schema(description = "배달 주소 등록 요청")
public record MemberDeliveryAddressCreateRequest(
    @Size(max = 50, message = "주소 별칭은 최대 50자까지 입력 가능합니다.")
    @Schema(description = "주소 별칭(집/회사 등)", example = "집")
    String alias,

    @NotBlank(message = "도로명 주소는 필수입니다.")
    @Size(max = 500, message = "도로명 주소는 최대 500자까지 입력 가능합니다.")
    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
    String roadAddress,

    @Size(max = 500, message = "지번 주소는 최대 500자까지 입력 가능합니다.")
    @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼1동 678-9")
    String lotAddress,

    @Size(max = 200, message = "상세 주소는 최대 200자까지 입력 가능합니다.")
    @Schema(description = "상세 주소", example = "101동 1001호")
    String detailAddress,

    @NotNull(message = "위도는 필수입니다.")
    @Schema(description = "위도. 주소 검색 API가 내려준 값을 그대로 보냅니다.", example = "37.501234", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal latitude,

    @NotNull(message = "경도는 필수입니다.")
    @Schema(description = "경도. 주소 검색 API가 내려준 값을 그대로 보냅니다.", example = "127.039876", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal longitude,

    @Schema(description = "기본 배송지 여부. true면 기존 기본 배송지는 자동으로 해제됩니다.", example = "true")
    Boolean isDefault
) {
    public MemberDeliveryAddressCreateCommand toCommand(Long memberId) {
        return new MemberDeliveryAddressCreateCommand(
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            latitude,
            longitude,
            isDefault
        );
    }
}
