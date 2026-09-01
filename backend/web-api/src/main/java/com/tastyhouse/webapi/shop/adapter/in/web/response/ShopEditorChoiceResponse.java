package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.EditorChoiceResult;

@Schema(description = "에디터 초이스 응답")
public record ShopEditorChoiceResponse(
    @Schema(description = "에디터 초이스 ID", example = "1")
    Long id,

    @Schema(description = "에디터명", example = "미식가 김테이")
    String name,

    @Schema(description = "에디터 이미지 URL", example = "https://cdn.tastyhouse.com/editor/1/profile.jpg")
    String imageUrl,

    @Schema(description = "제목", example = "이번 주 놓치면 안 될 맛집 추천")
    String title,

    @Schema(description = "본문 내용", example = "이번 주 새롭게 소개하는 맛집은...")
    String content,

    @Schema(description = "추천 상품 목록")
    List<ShopEditorChoiceProductItem> products
) {
    /**
     * {@code products}가 {@code null}이면 빈 배열로 내린다 — 추천 상품이 없는 초이스도 목록에 나와야
     * 하므로 응답 계약이 빈 배열이고, 이는 값을 만들지 않는 순수 null 기본값이다.
     */
    public static ShopEditorChoiceResponse from(EditorChoiceResult result) {
        return new ShopEditorChoiceResponse(
            result.id(),
            result.name(),
            result.shopImageUrl(),
            result.title(),
            result.content(),
            result.products() == null
                ? List.of()
                : result.products().stream().map(ShopEditorChoiceProductItem::from).toList()
        );
    }
}
