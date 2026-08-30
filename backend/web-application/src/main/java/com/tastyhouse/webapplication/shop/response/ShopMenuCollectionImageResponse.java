package com.tastyhouse.webapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴모음컷 목록 항목(손님용) — 가게를 열었을 때 가장 먼저, 가장 상단에서 보이는 이미지.
 *
 * <p><b>{@code status}·{@code rejectReason}이 없다.</b> 손님에게는 승인된 것만 내려가므로 상태 필드가
 * 무의미하고, 반려 사유는 점주에게만 의미 있는 내부 정보다. 점주 화면(ceo-api)의 동명 응답과 필드가
 * 다른 것은 소비자별 계약 차이이므로 공용화하지 않는다.
 */
@Schema(description = "메뉴모음컷 목록 항목")
public record ShopMenuCollectionImageResponse(
    @Schema(description = "메뉴모음컷 ID", example = "12")
    Long id,

    @Schema(description = "이미지의 표시용 URL. 파일이 없으면 null",
        example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2026%2F08%2F22%2Fmenu-collection.jpg?alt=media")
    String imageUrl,

    @Schema(description = "표시 순서(0부터 시작)", example = "0")
    Integer sort
) {

    public static ShopMenuCollectionImageResponse from(Long id, String imageUrl, Integer sort) {
        return new ShopMenuCollectionImageResponse(id, imageUrl, sort);
    }
}
