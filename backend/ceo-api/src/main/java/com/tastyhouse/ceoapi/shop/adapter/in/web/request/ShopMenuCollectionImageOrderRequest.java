package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageReorderCommand;

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
