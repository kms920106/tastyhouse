package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaRadiusApplyCommand;

/**
 * 반경으로 배달가능지역을 일괄 적용하는 요청.
 *
 * <p>{@code replace}가 {@code false}(기본)면 기존 설정 위에 더하고, {@code true}면 반경 밖의 기존
 * 행정동 직접 등록분을 닫고 교체한다. 기본값을 "더하기"로 두는 이유는, 실수로 보냈을 때 <b>기존 설정이
 * 사라지지 않는 쪽</b>이 안전하기 때문이다.
 */
@Schema(description = "반경 배달가능지역 적용 요청")
public record ShopDeliveryAreaRadiusRequest(
    @Min(value = 500, message = "반경은 500m 이상이어야 합니다.")
    @Max(value = 7000, message = "반경은 7000m를 넘을 수 없습니다.")
    @Schema(description = "반경(m)", example = "4000", requiredMode = Schema.RequiredMode.REQUIRED)
    int radiusMeters,

    @Schema(description = "기존 직접 등록분을 교체할지 여부(기본 false = 더하기)", example = "false")
    boolean replace
) {

    public ShopDeliveryAreaRadiusApplyCommand toCommand(Long ceoId, Long shopId) {
        return new ShopDeliveryAreaRadiusApplyCommand(ceoId, shopId, radiusMeters(), replace());
    }
}
