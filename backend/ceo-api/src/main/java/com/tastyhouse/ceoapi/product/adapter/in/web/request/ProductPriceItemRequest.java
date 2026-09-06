package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.product.port.in.ProductPriceItemCommand;

@Schema(description = "메뉴 가격 행")
public record ProductPriceItemRequest(
    @Schema(description = "가격 행 ID. 신규 추가면 비웁니다", example = "10")
    Long id,

    @Size(max = 50, message = "가격명은 50자 이하여야 합니다.")
    @Schema(description = "가격명(가격 행이 2개 이상이면 필수)", example = "대")
    String priceName,

    @NotNull(message = "배달가격은 필수입니다.")
    @PositiveOrZero(message = "배달가격은 0원 이상이어야 합니다.")
    @Schema(description = "배달가격(원)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer deliveryPrice,

    @PositiveOrZero(message = "매장가격은 0원 이상이어야 합니다.")
    @Schema(description = "매장가격(원). 매장 가격 인증을 받은 가게만 설정할 수 있습니다", example = "14000")
    Integer storePrice,

    @PositiveOrZero(message = "픽업가격은 0원 이상이어야 합니다.")
    @Schema(description = "픽업가격(원). 매장 가격 인증을 받은 가게만 설정할 수 있습니다", example = "14000")
    Integer pickupPrice,

    @NotNull(message = "표시 순서는 필수입니다.")
    @PositiveOrZero(message = "표시 순서는 0 이상이어야 합니다.")
    @Schema(description = "표시 순서(0부터). sort=0 행의 배달가가 메뉴 대표가로 동기화됩니다", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer sort
) {
    public ProductPriceItemCommand toCommand() {
        return new ProductPriceItemCommand(
            this.id(),
            this.priceName(),
            this.deliveryPrice(),
            this.storePrice(),
            this.pickupPrice(),
            this.sort()
        );
    }
}
