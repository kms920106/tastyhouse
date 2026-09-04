package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageReorderCommand;

/**
 * 메뉴모음컷 순서 변경 요청.
 *
 * <p>"몇 번째로 옮길지"가 아니라 <b>화면에 보이는 순서대로 나열한 전체 id 목록</b>을 받는다(replace-all).
 * 개별 위치를 지정하는 방식은 동시 편집 시 두 이미지가 같은 순서를 갖는 상태를 만들 수 있다. 서버가
 * 이 목록으로 {@code 0..N-1}을 다시 부여하며, 목록이 현재 상태와 집합으로 다르면 거절한다.
 */
@Schema(description = "메뉴모음컷 순서 변경 요청")
public record ShopMenuCollectionImageOrderRequest(
    @NotEmpty(message = "이미지 ID 목록은 필수입니다.")
    @Schema(description = "표시할 순서대로 나열한 메뉴모음컷 ID 전체 목록", example = "[3, 1, 2]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> imageIds
) {

    public ShopMenuCollectionImageReorderCommand toCommand(Long ceoId, Long shopId) {
        return new ShopMenuCollectionImageReorderCommand(ceoId, shopId, imageIds());
    }
}
