package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductRepresentativeRequestCommand;

@Schema(description = "사장님 추천 메뉴 지정 요청")
public record ProductRepresentativeCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "지정할 메뉴를 1개 이상 선택해야 합니다.")
    @Schema(description = "사장님 추천으로 지정할 메뉴 ID 목록. 이미 추천이거나 대기 중인 메뉴는 건너뜁니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productIds
) {
    public ProductRepresentativeRequestCommand toCommand(Long ceoId) {
        return new ProductRepresentativeRequestCommand(ceoId, shopId, productIds);
    }
}
