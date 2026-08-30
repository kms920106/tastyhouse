package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupUpdateCommand;

/**
 * 옵션그룹 변경 요청.
 *
 * <p>등록 요청과 달리 {@code productId}를 받지 않는다 — 그룹이 어느 메뉴에 연결되는지는 연결 API
 * ({@code ProductOptionGroupLinkApiController})의 관심사이고, 이 경로로 바꾸면 연결 집합이 두 곳에서
 * 변경돼 단일 가게 불변식 검증이 흩어진다. 순서도 마찬가지로 이 경로로 바꾸지 않는다.
 */
@Schema(description = "옵션그룹 변경 요청")
public record ProductOptionGroupUpdateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "옵션그룹명은 필수입니다.")
    @Size(max = 100, message = "옵션그룹명은 100자 이하여야 합니다.")
    @Schema(description = "옵션그룹명", example = "맵기 선택", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 500, message = "옵션그룹 설명은 500자 이하여야 합니다.")
    @Schema(description = "옵션그룹 설명", example = "원하시는 맵기를 골라주세요.")
    String description,

    @NotNull(message = "필수 선택 여부는 필수입니다.")
    @Schema(description = "필수 선택 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean required,

    @NotNull(message = "다중 선택 여부는 필수입니다.")
    @Schema(description = "다중 선택 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean multipleSelect,

    @Min(value = 0, message = "최소 선택 개수는 0 이상이어야 합니다.")
    @Schema(description = "최소 선택 개수. null이면 미지정. 최대 선택 개수보다 클 수 없다.", example = "1")
    Integer minSelect,

    @Min(value = 0, message = "최대 선택 개수는 0 이상이어야 합니다.")
    @Schema(description = "최대 선택 개수. null이면 미지정(무제한)", example = "3")
    Integer maxSelect
) {

    public ProductOptionGroupUpdateCommand toCommand(Long ceoId, Long optionGroupId) {
        return new ProductOptionGroupUpdateCommand(
            ceoId,
            optionGroupId,
            this.shopId(),
            this.name(),
            this.description(),
            this.required(),
            this.multipleSelect(),
            this.minSelect(),
            this.maxSelect()
        );
    }
}
