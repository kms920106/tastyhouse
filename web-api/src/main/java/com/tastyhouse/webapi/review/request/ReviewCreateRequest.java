package com.tastyhouse.webapi.review.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "리뷰 등록 요청")
public record ReviewCreateRequest(

    @Schema(description = "주문 상품 ID (주문 기반 리뷰 시 필수, 일반 리뷰 시 null)", example = "10")
    Long orderItemId,

    @NotNull(message = "상품 ID는 필수입니다")
    @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long productId,

    @NotNull(message = "맛 평점은 필수입니다")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다")
    @Schema(description = "맛 평점 (1~5)", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer tasteRating,

    @NotNull(message = "양 평점은 필수입니다")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다")
    @Schema(description = "양 평점 (1~5)", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer amountRating,

    @NotNull(message = "가격 평점은 필수입니다")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다")
    @Schema(description = "가격 평점 (1~5)", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer priceRating,

    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(max = 500, message = "리뷰 내용은 500자 이내로 입력해주세요")
    @Schema(description = "리뷰 내용", example = "정말 맛있었어요!", requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @Size(max = 5, message = "이미지는 최대 5장까지 첨부할 수 있습니다")
    @Schema(description = "업로드된 파일 ID 목록 (최대 5장)", example = "[1, 2, 3]")
    List<Long> uploadedFileIds,

    @Schema(description = "태그 목록", example = "[\"샌드위치\", \"아보카도\"]")
    List<String> tags
) {
}
