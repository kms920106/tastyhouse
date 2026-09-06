package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopCeoAssignCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 담당 점주 배정 요청")
public record ShopCeoAssignRequest(

    @NotNull(message = "점주 ID는 필수입니다.")
    @Schema(description = "배정할 점주 ID", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
    Long ceoId
) {
    public ShopCeoAssignCommand toCommand(Long adminId, Long shopId) {
        return new ShopCeoAssignCommand(adminId, shopId, ceoId);
    }
}
