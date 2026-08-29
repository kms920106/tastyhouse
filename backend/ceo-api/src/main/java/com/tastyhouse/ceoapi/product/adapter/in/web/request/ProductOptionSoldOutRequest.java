package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionSoldOutCommand;

@Schema(description = "옵션 일괄 품절 요청")
public record ProductOptionSoldOutRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Valid
    @NotEmpty(message = "대상을 1개 이상 선택해야 합니다.")
    @Size(max = 200, message = "한 번에 200개까지 처리할 수 있습니다.")
    @Schema(description = "품절 처리할 옵션 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ProductOptionTargetRequest> options,

    @Schema(description = "품절 자동해제 시각. 미지정이면 서버가 다음 영업일 오픈 시각으로 채운다. "
        + "지정 시 현재+30분 ~ 현재+7일 범위여야 한다.", example = "2026-08-18T09:00:00")
    LocalDateTime soldOutUntil
) {

    public ProductOptionSoldOutCommand toCommand(Long ceoId) {
        return new ProductOptionSoldOutCommand(
            ceoId,
            this.shopId(),
            this.options() == null ? null : this.options().stream().map(ProductOptionTargetRequest::toCommand).toList(),
            this.soldOutUntil()
        );
    }
}
