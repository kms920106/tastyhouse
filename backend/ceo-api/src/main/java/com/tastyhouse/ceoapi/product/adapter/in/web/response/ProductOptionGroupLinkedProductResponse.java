package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 옵션그룹을 사용하는 메뉴 — 연결 해제 전 영향 확인 화면에 쓴다.
 *
 * <p>해제하려는 그룹이 다른 메뉴에서도 쓰이고 있는지, 그리고 지금이 마지막 연결인지를 점주가
 * 먼저 알 수 있게 한다(마지막 연결 해제는 서버가 거부한다).
 */
@Schema(description = "옵션그룹을 사용하는 메뉴")
public record ProductOptionGroupLinkedProductResponse(
    @Schema(description = "메뉴 ID", example = "100")
    Long id,

    @Schema(description = "메뉴명", example = "매운 등갈비")
    String name
) {

    public static ProductOptionGroupLinkedProductResponse from(Long id, String name) {
        return new ProductOptionGroupLinkedProductResponse(id, name);
    }
}
