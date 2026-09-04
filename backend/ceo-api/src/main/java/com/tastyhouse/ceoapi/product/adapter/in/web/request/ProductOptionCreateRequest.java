package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapplication.product.port.in.ProductOptionOwnerCreateCommand;

@Schema(description = "옵션 등록 요청")
public record ProductOptionCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "옵션명은 필수입니다.")
    @Size(max = 100, message = "옵션명은 100자 이하여야 합니다.")
    @Schema(description = "옵션명", example = "아주 매운맛", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "추가 금액은 필수입니다.")
    @Min(value = 0, message = "추가 금액은 0 이상이어야 합니다.")
    @Schema(description = "추가 금액(원). 무료 옵션은 0", example = "500",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Integer additionalPrice,

    // 범위(1~10) 검증은 Bean Validation이 아니라 도메인 계층(CupDepositPolicy#validateCupCount)이 소유한다.
    // 여기에 @Min/@Max를 다시 붙이면 경계별로 다른 문구가 나가, ErrorCode.PRODUCT_OPTION_CUP_COUNT_INVALID의
    // 통합 메시지("1개 이상 10개 이하")와 어긋난다.
    @Schema(description = "일회용컵 제공 개수(1~10). 보증금 옵션그룹의 옵션만 값을 갖습니다. "
        + "보증금액은 개수 × 300원으로 서버가 계산하므로 금액을 직접 보내지 않습니다.",
        example = "1")
    Integer cupCount,

    @Min(value = 0, message = "개인컵 할인 금액은 0원 이상이어야 합니다.")
    @Schema(description = "개인컵 사용 할인 금액(원). 보증금 옵션그룹 안에서만 설정할 수 있습니다. "
        + "보증금이 아니라 상품 할인 축이며, 이 값이 있는 옵션은 컵을 제공하지 않으므로 cupCount가 없습니다.",
        example = "300")
    Integer personalCupDiscountAmount
) {

    /**
     * 같은 타입의 금액·수량 필드가 연달아 있어 위치 기반 조립은 뒤바뀜을 컴파일러가 잡지 못한다.
     * 반드시 이름 기반 접근자로 조립한다.
     */
    public ProductOptionOwnerCreateCommand toCommand(Long ceoId, Long optionGroupId) {
        return new ProductOptionOwnerCreateCommand(
            ceoId,
            this.shopId(),
            optionGroupId,
            this.name(),
            this.additionalPrice(),
            this.cupCount(),
            this.personalCupDiscountAmount()
        );
    }
}
