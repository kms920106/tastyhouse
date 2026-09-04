package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.application.product.port.in.ProductOptionGroupOwnerCreateCommand;

/**
 * 옵션그룹 등록 요청.
 *
 * <p>{@code productId}가 필수인 이유: {@code PRODUCT_OPTION_GROUP.product_id}가 1단계 배포 동안
 * {@code NOT NULL}로 남아 있고({@code product-menu-management.sql} STEP 6에서 제거 예정),
 * 무엇보다 <b>연결이 0건인 그룹은 어느 화면에서도 보이지 않는 고아</b>가 된다. 등록 시 이 메뉴에
 * 곧바로 연결해 그룹이 항상 소유 가게로 역조회되도록 보장한다.
 */
@Schema(description = "옵션그룹 등록 요청")
public record ProductOptionGroupCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotNull(message = "메뉴 ID는 필수입니다.")
    @Schema(description = "이 옵션그룹을 최초로 연결할 메뉴 ID. 연결 0건인 고아 그룹을 만들지 않기 위해 "
        + "필수다.", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    Long productId,

    @NotBlank(message = "옵션그룹명은 필수입니다.")
    @Size(max = 100, message = "옵션그룹명은 100자 이하여야 합니다.")
    @Schema(description = "옵션그룹명", example = "맵기 선택", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 500, message = "옵션그룹 설명은 500자 이하여야 합니다.")
    @Schema(description = "옵션그룹 설명", example = "원하시는 맵기를 골라주세요.")
    String description,

    @NotNull(message = "필수 선택 여부는 필수입니다.")
    @Schema(description = "필수 선택 여부. true면 주문 시 반드시 하나 이상 골라야 한다.", example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean required,

    @NotNull(message = "다중 선택 여부는 필수입니다.")
    @Schema(description = "다중 선택 여부. false면 하나만 고를 수 있다.", example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean multipleSelect,

    @Min(value = 0, message = "최소 선택 개수는 0 이상이어야 합니다.")
    @Schema(description = "최소 선택 개수. null이면 미지정. 최대 선택 개수보다 클 수 없다.", example = "1")
    Integer minSelect,

    @Min(value = 0, message = "최대 선택 개수는 0 이상이어야 합니다.")
    @Schema(description = "최대 선택 개수. null이면 미지정(무제한)", example = "3")
    Integer maxSelect,

    @Schema(description = "옵션그룹 유형. 미지정이면 NORMAL입니다. CUP_DEPOSIT은 일회용컵 보증금제 "
        + "대상 가게(cupDepositEnabled=true)만 만들 수 있고, 필수 선택 불가·minSelect=0·maxSelect=1·"
        + "multipleSelect=false로 고정됩니다.",
        example = "NORMAL", allowableValues = {"NORMAL", "CUP_DEPOSIT"})
    String groupType
) {

    public ProductOptionGroupOwnerCreateCommand toCommand(Long ceoId) {
        return new ProductOptionGroupOwnerCreateCommand(
            ceoId,
            this.shopId(),
            this.productId(),
            this.name(),
            this.description(),
            this.required(),
            this.multipleSelect(),
            this.minSelect(),
            this.maxSelect(),
            this.groupType()
        );
    }
}
