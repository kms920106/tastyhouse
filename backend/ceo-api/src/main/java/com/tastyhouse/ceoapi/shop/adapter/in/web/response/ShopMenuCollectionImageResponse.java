package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageResult;

@Schema(description = "메뉴모음컷 목록 항목")
public record ShopMenuCollectionImageResponse(
    @Schema(description = "메뉴모음컷 ID", example = "12")
    Long id,

    @Schema(description = "이미지의 표시용 URL. 파일이 없으면 null",
        example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2026%2F08%2F22%2Fmenu-collection.jpg?alt=media")
    String imageUrl,

    @Schema(description = "표시 순서(0부터 시작)", example = "0")
    Integer sort,

    @Schema(description = "승인 상태", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "메뉴가 잘 보이지 않습니다.")
    String rejectReason
) {
    public static ShopMenuCollectionImageResponse from(ShopMenuCollectionImageResult result) {
        return new ShopMenuCollectionImageResponse(
            result.id(),
            result.imageUrl(),
            result.sort(),
            result.status().name(),
            result.rejectReason()
        );
    }
}
