package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapi.product.application.port.in.ProductOptionReleaseCommand;

@Schema(description = "옵션 일괄 품절·숨김 해제 요청")
public record ProductOptionReleaseRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Valid
    @NotEmpty(message = "대상을 1개 이상 선택해야 합니다.")
    @Size(max = 200, message = "한 번에 200개까지 처리할 수 있습니다.")
    @Schema(description = "해제할 옵션 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ProductOptionTargetRequest> options,

    @NotBlank(message = "해제 대상은 필수입니다.")
    @Schema(description = "해제 대상. ALL은 품절과 숨김을 함께 푼다.", example = "ALL",
        allowableValues = {"SOLD_OUT", "HIDDEN", "ALL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String target
) {

    public ProductOptionReleaseCommand toCommand(Long ceoId) {
        return new ProductOptionReleaseCommand(
            ceoId,
            this.shopId(),
            this.options() == null ? null : this.options().stream().map(ProductOptionTargetRequest::toCommand).toList(),
            this.target()
        );
    }
}
