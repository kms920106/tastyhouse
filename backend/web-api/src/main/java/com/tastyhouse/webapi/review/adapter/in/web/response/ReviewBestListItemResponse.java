package com.tastyhouse.webapi.review.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.BestReviewListItemResult;

@Schema(description = "베스트 리뷰 목록 아이템 응답")
public record ReviewBestListItemResponse(
    @Schema(description = "리뷰 ID", example = "1")
    Long id,

    @Schema(description = "리뷰 이미지 URL", example = "https://cdn.tastyhouse.com/review/1/image.jpg")
    String imageUrl,

    @Schema(description = "역/지점명", example = "강남역점")
    String stationName,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String shopName,

    @Schema(description = "상품명", example = "황금올리브치킨")
    String productName,

    @Schema(description = "총점", example = "4.5")
    Double totalRating,

    @Schema(description = "리뷰 내용", example = "맛있게 잘 먹었습니다.")
    String content
) {
    public static ReviewBestListItemResponse from(BestReviewListItemResult result) {
        return new ReviewBestListItemResponse(
            result.id(),
            result.imageUrl(),
            result.stationName(),
            result.shopName(),
            result.productName(),
            result.totalRating(),
            result.content()
        );
    }
}
