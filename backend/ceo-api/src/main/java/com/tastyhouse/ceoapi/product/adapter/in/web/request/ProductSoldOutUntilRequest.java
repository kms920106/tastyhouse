package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapi.product.application.port.in.ProductSoldOutUntilChangeCommand;

@Schema(description = "메뉴 품절 기간 일괄 변경 요청")
public record ProductSoldOutUntilRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "대상을 1개 이상 선택해야 합니다.")
    @Size(max = 200, message = "한 번에 200개까지 처리할 수 있습니다.")
    @Schema(description = "기간을 변경할 메뉴 ID 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productIds,

    @NotNull(message = "품절 기간은 필수입니다.")
    @Schema(description = "변경할 품절 자동해제 시각. 현재+30분 ~ 현재+7일 범위여야 한다.",
        example = "2026-08-18T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime soldOutUntil
) {

    public ProductSoldOutUntilChangeCommand toCommand(Long ceoId) {
        return new ProductSoldOutUntilChangeCommand(ceoId, shopId, productIds, soldOutUntil);
    }
}
