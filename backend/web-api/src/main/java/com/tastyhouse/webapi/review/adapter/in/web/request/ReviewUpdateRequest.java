package com.tastyhouse.webapi.review.adapter.in.web.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapplication.review.port.in.ReviewUpdateCommand;

@Schema(description = "리뷰 수정 요청")
public record ReviewUpdateRequest(

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

    @Schema(description = "태그 목록 (기존 태그 포함하여 전체 목록 전달)", example = "[\"샌드위치\", \"아보카도\"]")
    List<String> tags,

    @Min(value = 1, message = "배달 평점은 1 이상이어야 합니다")
    @Max(value = 5, message = "배달 평점은 5 이하이어야 합니다")
    @Schema(
        description = "배달 평점 (1~5, 선택). 주문유형이 DELIVERY인 주문 기반 리뷰에만 보낼 수 있으며, "
            + "그 외 주문유형에 값을 보내면 REVIEW_DELIVERY_RATING_NOT_ALLOWED(400)로 거부됩니다. "
            + "작성된 배달 평가는 점주에게만 노출되고 고객 화면에는 표시되지 않습니다.",
        example = "5"
    )
    Integer deliveryRating,

    @Size(max = 500, message = "배달 평가 내용은 500자 이내로 입력해주세요")
    @Schema(description = "배달 평가 내용 (선택, 점주 전용 노출)", example = "빠르게 잘 받았어요")
    String deliveryComment
) {

    /**
     * 인증 주체의 {@code memberId}와 경로 변수 {@code reviewId}를 주입받아 command로 변환한다.
     *
     * <p>평점 3종이 같은 {@code Integer}라 위치 기반 전달은 조용히 뒤바뀌므로, 아래는 이름 기반
     * 접근자로 각 값을 짚어 넘긴다.
     */
    public ReviewUpdateCommand toCommand(Long memberId, Long reviewId) {
        return new ReviewUpdateCommand(
            memberId,
            reviewId,
            tasteRating,
            amountRating,
            priceRating,
            content,
            uploadedFileIds,
            tags,
            deliveryRating,
            deliveryComment
        );
    }
}
