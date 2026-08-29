package com.tastyhouse.webapi.order.adapter.in.web.request;

import java.util.List;

import com.tastyhouse.webapi.order.application.port.in.OrderLineCommand;
import com.tastyhouse.webapi.order.application.port.in.OrderLineOptionCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상품 요청")
public record OrderProductRequest(
    @NotNull(message = "상품 ID는 필수입니다")
    @Schema(description = "상품 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    Long productId,

    @Schema(description = "선택한 가격 ID. 가격명이 여러 개인 메뉴에서 손님이 고른 가격 행입니다. "
        + "미지정이면 기본 가격(sort가 가장 작은 행)이 적용됩니다. "
        + "어느 채널 가격(배달가·픽업가)을 쓸지는 서버가 주문유형으로 단독 결정하므로 이 값으로 지정할 수 없습니다.",
        example = "5")
    Long priceId,

    @Valid
    @Schema(description = "선택한 옵션 목록")
    List<OrderProductOptionRequest> options,

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "수량", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer quantity
) {

    public OrderLineCommand toCommand() {
        List<OrderLineOptionCommand> optionCommands = options == null ? null :
            options.stream()
                .map(OrderProductOptionRequest::toCommand)
                .toList();
        return OrderLineCommand.of(
            productId,
            priceId,
            optionCommands,
            quantity
        );
    }
}
