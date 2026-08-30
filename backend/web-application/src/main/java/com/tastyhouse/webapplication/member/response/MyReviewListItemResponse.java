package com.tastyhouse.webapplication.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 리뷰 목록 아이템")
public record MyReviewListItemResponse(
    @Schema(description = "리뷰 ID(PK)", example = "1")
    Long id,

    @Schema(description = "리뷰 이미지 URL", example = "https://cdn.tastyhouse.com/review/image/1.jpg")
    String imageUrl,

    @Schema(description = "사장님만보기 여부. 마이페이지는 본인 한정 조회라 비공개 리뷰도 포함되므로, 뱃지 표시에 사용합니다.", example = "false")
    boolean ownerOnly
) {
    public static MyReviewListItemResponse from(
        Long reviewId,
        String imageUrl,
        boolean ownerOnly
    ) {
        return new MyReviewListItemResponse(
            reviewId,
            imageUrl,
            ownerOnly
        );
    }
}
