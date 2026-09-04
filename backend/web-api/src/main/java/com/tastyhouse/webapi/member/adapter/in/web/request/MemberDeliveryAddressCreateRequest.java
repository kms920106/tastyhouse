package com.tastyhouse.webapi.member.adapter.in.web.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.member.port.in.MemberDeliveryAddressCreateCommand;

/**
 * 배달 주소 등록 요청.
 *
 * <p>좌표는 클라이언트가 주소 검색 API에서 받은 값을 그대로 보낸다. 좌표가 없으면 거리별 배달팁을
 * 산출할 수 없어 할증이 0원이 되므로 <b>필수</b>다. 행정동은 서버가 주소 문자열로 매칭해 채우므로
 * 요청 필드에 없다.
 */
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

    /**
     * 인증 주체의 {@code memberId}를 주입받아 command로 변환한다.
     *
     * <p>주소 {@code String} 4개와 좌표 {@code BigDecimal} 2개가 각각 연달아 있어 위치 기반 전달은
     * 조용히 뒤바뀐다(위경도가 뒤바뀌면 배달팁이 엉뚱하게 산출된다) — 아래는 이름 기반 접근자로
     * 각 값을 짚어 넘긴다.
     */
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
