package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.product.port.in.ProductOptionGroupOrderChangeCommand;

/**
 * 메뉴 안에서의 옵션그룹 순서 변경 요청(replace-all).
 *
 * <p>{@code sort} 값을 받지 않는다 — 순서 있는 id 배열만 받아 서버가 배열 인덱스로 {@code 0..N-1}을
 * 부여한다. 순서는 그룹이 아니라 <b>링크</b>가 갖기 때문에 같은 그룹도 메뉴마다 순서가 다를 수 있다.
 */
@Schema(description = "메뉴 내 옵션그룹 순서 변경 요청")
public record ProductOptionGroupSortRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "옵션그룹 ID 목록은 비어 있을 수 없습니다.")
    @Schema(description = "화면에 보이는 순서대로 나열한 옵션그룹 ID 전체 목록. 이 메뉴에 연결된 현재 "
        + "옵션그룹 집합과 일치해야 한다.", example = "[3, 1, 7]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> optionGroupIds
) {

    public ProductOptionGroupOrderChangeCommand toCommand(Long ceoId, Long productId) {
        return new ProductOptionGroupOrderChangeCommand(ceoId, shopId, productId, optionGroupIds);
    }
}
