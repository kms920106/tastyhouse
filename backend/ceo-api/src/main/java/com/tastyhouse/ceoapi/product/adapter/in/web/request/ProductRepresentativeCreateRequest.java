package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapi.product.application.port.in.ProductRepresentativeRequestCommand;

/**
 * 사장님 추천(대표 메뉴) 지정 요청.
 *
 * <p>여러 메뉴를 한 번에 신청하는 이유는 <b>개수 제한이 집합 단위 불변식</b>이기 때문이다 — 최대 6개
 * 판정은 요청 전체를 반영한 뒤의 최종 상태를 봐야 하고, 한 건씩 받으면 어느 건이 통과할지가 호출
 * 순서에 좌우된다.
 *
 * <p>개수 상한은 Bean Validation으로 가로채지 않는다({@code @Size(max = 6)}을 붙이지 않는다) —
 * "6개 초과"가 400 검증 오류로 걸리고 "이미 5개 있는데 2개 추가"는 도메인 에러코드로 내려가면 같은
 * 개수 위반이 상황에 따라 다른 {@code code}로 응답되어 프론트 분기가 갈린다. 판정은 도메인 한 곳
 * ({@code PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED})에 맡긴다.
 */
@Schema(description = "사장님 추천 메뉴 지정 요청")
public record ProductRepresentativeCreateRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotEmpty(message = "지정할 메뉴를 1개 이상 선택해야 합니다.")
    @Schema(description = "사장님 추천으로 지정할 메뉴 ID 목록. 이미 추천이거나 대기 중인 메뉴는 건너뜁니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> productIds
) {

    public ProductRepresentativeRequestCommand toCommand(Long ceoId) {
        return new ProductRepresentativeRequestCommand(ceoId, shopId, productIds);
    }
}
