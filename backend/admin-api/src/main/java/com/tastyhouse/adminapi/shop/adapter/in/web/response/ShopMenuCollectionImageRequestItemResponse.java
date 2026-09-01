package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;

/**
 * 메뉴모음컷 검수 목록 항목.
 *
 * <p>파일 식별자 대신 표시용 URL을 담는다 — 검수자가 이미지를 눈으로 확인해야 하고, 프론트엔드가
 * fileId로 URL을 조립할 공식 경로가 없다. {@code shopName}을 함께 담는 이유는 가게 식별자만으로는
 * 검수자가 어느 가게 요청인지 판단할 수 없기 때문이다.
 */
@Schema(description = "메뉴모음컷 검수 목록 항목")
public record ShopMenuCollectionImageRequestItemResponse(
    @Schema(description = "메뉴모음컷 ID", example = "12")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는집 강남점")
    String shopName,

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

    public static ShopMenuCollectionImageRequestItemResponse from(ShopMenuCollectionImageRequestResult result) {
        return new ShopMenuCollectionImageRequestItemResponse(
            result.id(),
            result.shopId(),
            result.shopName(),
            result.imageUrl(),
            result.sort(),
            result.status().name(),
            result.rejectReason()
        );
    }
}
