package com.tastyhouse.webapi.member.adapter.in.web.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapi.member.application.port.in.MemberDeliveryAddressUpdateCommand;

/**
 * 배달 주소 수정 요청.
 *
 * <p>등록과 같은 필드 구성이며 좌표도 동일하게 필수다 — 생성만 막고 수정을 열어두면 좌표 없는 주소가
 * 뒷문으로 들어온다. 기본 배송지 지정은 이 요청이 아니라 전용 엔드포인트
 * ({@code PATCH /v1/me/delivery-addresses/{id}/default})가 담당하므로 {@code isDefault}가 없다.
 */
@Schema(description = "배달 주소 수정 요청")
public record MemberDeliveryAddressUpdateRequest(
    @Size(max = 50, message = "주소 별칭은 최대 50자까지 입력 가능합니다.")
    @Schema(description = "주소 별칭(집/회사 등)", example = "회사")
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
    BigDecimal longitude
) {

    /**
     * 인증 주체의 {@code memberId}와 경로 변수 {@code addressId}를 주입받아 command로 변환한다.
     *
     * <p>주소 {@code String} 4개와 좌표 {@code BigDecimal} 2개가 각각 연달아 있어 위치 기반 전달은
     * 조용히 뒤바뀌므로, 아래는 이름 기반 접근자로 각 값을 짚어 넘긴다.
     */
    public MemberDeliveryAddressUpdateCommand toCommand(Long memberId, Long addressId) {
        return new MemberDeliveryAddressUpdateCommand(
            memberId,
            addressId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            latitude,
            longitude
        );
    }
}
