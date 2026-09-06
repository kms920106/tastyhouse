package com.tastyhouse.adminapi.product.adapter.in.web.request;

import com.tastyhouse.application.product.port.in.ProductImageChangeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductRepresentativeRejectCommand;
import com.tastyhouse.application.product.port.in.ProductVegetarianRejectCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 승인요청 반려 요청")
public record ProductApprovalRejectRequest(
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다.")
    @Schema(description = "반려 사유", example = "메뉴가 잘 보이지 않습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String rejectReason
) {
    public ProductImageChangeRejectCommand toImageChangeCommand(Long requestId) {
        return new ProductImageChangeRejectCommand(requestId, rejectReason);
    }

    public ProductVegetarianRejectCommand toVegetarianCommand(Long requestId) {
        return new ProductVegetarianRejectCommand(requestId, rejectReason);
    }

    public ProductRepresentativeRejectCommand toRepresentativeCommand(Long requestId) {
        return new ProductRepresentativeRejectCommand(requestId, rejectReason);
    }
}
