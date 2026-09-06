package com.tastyhouse.webapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "상품 배치 조회 요청. (상품ID, 옵션ID) 조합의 목록입니다.")
public record ProductBatchRequest(
    @Schema(description = "조회할 항목 목록")
    @NotEmpty(message = "조회할 항목 목록은 비어 있을 수 없습니다.")
    @Size(max = 200, message = "한 번에 조회할 수 있는 항목은 최대 200개입니다.")
    @Valid
    List<BatchItemRequest> items,

    @Schema(description = "가격을 해석할 주문유형. 미지정이면 DELIVERY로 조회합니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "TAKEOUT")
    String orderMethod
) {
    private static final String DEFAULT_ORDER_METHOD = "DELIVERY";

    public ProductBatchRequest {
        if (orderMethod == null || orderMethod.isBlank()) {
            orderMethod = DEFAULT_ORDER_METHOD;
        }
    }

    public com.tastyhouse.application.product.port.in.ProductBatchQuery toQuery() {
        return new com.tastyhouse.application.product.port.in.ProductBatchQuery(
            items.stream()
                .map(item -> new com.tastyhouse.application.product.port.in.ProductBatchQuery.Item(
                    item.productId(), item.optionId()))
                .toList(),
            orderMethod
        );
    }
}
