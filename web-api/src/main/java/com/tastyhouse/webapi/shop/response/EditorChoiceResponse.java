package com.tastyhouse.webapi.shop.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에디터 초이스 응답")
public record EditorChoiceResponse(
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
    List<EditorChoiceProductItem> products
) {
    public static EditorChoiceResponse from(
        Long id,
        String name,
        String imageUrl,
        String title,
        String content,
        List<EditorChoiceProductItem> products
    ) {
        return new EditorChoiceResponse(
            id,
            name,
            imageUrl,
            title,
            content,
            products
        );
    }
}
