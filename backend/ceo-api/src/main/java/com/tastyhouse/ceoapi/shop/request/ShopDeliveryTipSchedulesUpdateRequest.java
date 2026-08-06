package com.tastyhouse.ceoapi.shop.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 시간별 추가 배달팁 일괄 교체 요청.
 *
 * <p>빈 배열은 "전부 삭제"를 뜻하는 정상 입력이므로 {@code @NotEmpty}를 쓰지 않는다.
 */
@Schema(description = "시간별 추가 배달팁 일괄 교체 요청")
public record ShopDeliveryTipSchedulesUpdateRequest(
    @NotNull(message = "시간대 목록은 필수입니다.")
    @Valid
    @Schema(description = "시간별 배달팁 목록. 빈 배열이면 전부 삭제됩니다", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ShopDeliveryTipScheduleItemRequest> schedules
) {
}
