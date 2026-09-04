package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductOptionOrderChangeCommand;

/**
 * 옵션그룹 내 옵션 순서 변경 요청(replace-all).
 *
 * <p>{@code sort} 값을 받지 않는다 — 순서 있는 id 배열만 받아 서버가 배열 인덱스로 {@code 0..N-1}을
 * 부여하므로 "sort 충돌"이라는 개념 자체가 존재하지 않는다.
 */
@Schema(description = "옵션 순서 변경 요청")
public record ProductOptionSortRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "옵션 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "화면에 보이는 순서대로 나열한 옵션 ID 전체 목록. 이 옵션그룹의 현재 옵션 "
        + "집합과 일치해야 한다.", example = "[5, 2, 9]", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> optionIds
) {

    public ProductOptionOrderChangeCommand toCommand(Long ceoId, Long optionGroupId) {
        return new ProductOptionOrderChangeCommand(ceoId, shopId, optionGroupId, optionIds);
    }
}
