package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapi.shop.application.port.in.ShopRiderPickupLocationUpdateCommand;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 라이더 픽업 위치 교정 요청(라이더 제보 반영).
 *
 * <p>좌표 범위 판정은 도메인({@code ShopRiderGuide})이 담당한다 — 점주 경로(ceo-api)와 같은 게이트가
 * 적용되어야 하므로 Request로 끌어올리지 않는다.
 */
@Schema(description = "라이더 픽업 위치 교정 요청")
public record ShopRiderPickupLocationUpdateRequest(
    @NotBlank(message = "픽업 도로명주소는 필수입니다.")
    @Size(max = 255, message = "픽업 도로명주소는 최대 255자까지 입력할 수 있습니다.")
    @Schema(description = "픽업 도로명주소", example = "서울시 강남구 테헤란로 1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String roadAddress,

    @Size(max = 255, message = "픽업 지번주소는 최대 255자까지 입력할 수 있습니다.")
    @Schema(description = "픽업 지번주소", example = "서울시 강남구 역삼동 1-1")
    String lotAddress,

    @Size(max = 100, message = "픽업 상세주소는 최대 100자까지 입력할 수 있습니다.")
    @Schema(description = "픽업 상세주소 (동/호수 등, 최대 100자)", example = "지하 1층 후문")
    String detailAddress,

    @NotNull(message = "픽업 위도는 필수입니다.")
    @Schema(description = "픽업 위도 (-90 ~ 90)", example = "37.497942",
        requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal latitude,

    @NotNull(message = "픽업 경도는 필수입니다.")
    @Schema(description = "픽업 경도 (-180 ~ 180)", example = "127.027621",
        requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal longitude
) {

    public ShopRiderPickupLocationUpdateCommand toCommand(Long shopId, Long adminId) {
        return new ShopRiderPickupLocationUpdateCommand(
            shopId, adminId, roadAddress, lotAddress, detailAddress, latitude, longitude
        );
    }
}
