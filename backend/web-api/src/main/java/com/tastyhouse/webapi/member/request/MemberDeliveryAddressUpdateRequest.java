package com.tastyhouse.webapi.member.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
}
