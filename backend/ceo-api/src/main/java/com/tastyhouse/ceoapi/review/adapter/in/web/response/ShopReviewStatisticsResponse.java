package com.tastyhouse.ceoapi.review.adapter.in.web.response;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ShopReviewStatisticsOwnerResult;

/**
 * 점주 리뷰 통계 대시보드 응답.
 *
 * <p>{@code hasData}가 {@code false}면(최근 180일 리뷰 0건) 나머지는 전부 {@code null}·빈 값이다 —
 * 원문이 "180일간 리뷰가 없으면 대시보드를 노출하지 않는다"로 규정하므로, 0으로 채운 그래프를 보여주는
 * 대신 화면이 통째로 빈 상태를 렌더링할 수 있게 한다.
 */
@Schema(description = "점주 리뷰 통계")
public record ShopReviewStatisticsResponse(
    @Schema(
        description = "최근 180일 리뷰 존재 여부. false면 나머지 필드는 전부 null 또는 빈 값입니다.",
        example = "true"
    )
    Boolean hasData,

    @Schema(description = "최근 6개월 평균 종합 평점(소수 1자리)", example = "4.3")
    Double averageTotalRating,

    @Schema(description = "최근 6개월 리뷰 수", example = "128")
    Long totalReviewCount,

    @Schema(description = "최근 30일 리뷰 수", example = "17")
    Long recentReviewCount,

    @Schema(description = "별점별 리뷰 수. 키 1~5가 항상 존재하며 0건이면 값이 0입니다.")
    Map<Integer, Long> ratingCounts,

    @Schema(description = "맛 평균", example = "4.5")
    Double averageTasteRating,

    @Schema(description = "양 평균", example = "4.1")
    Double averageAmountRating,

    @Schema(description = "가격 평균", example = "3.9")
    Double averagePriceRating,

    @Schema(description = "분위기 평균", example = "4.2")
    Double averageAtmosphereRating,

    @Schema(description = "친절 평균", example = "4.6")
    Double averageKindnessRating,

    @Schema(description = "위생 평균", example = "4.4")
    Double averageHygieneRating,

    @Schema(description = "재방문 의사 비율(%)", example = "87.5")
    Double willRevisitPercentage,

    @Schema(description = "최근 6개월 월별 통계. 정확히 6개이며 오래된 달에서 최신 달 순입니다.")
    List<ShopReviewMonthlyStatResponse> monthlyStats
) {

    public static ShopReviewStatisticsResponse from(ShopReviewStatisticsOwnerResult result) {
        if (!result.hasData()) {
            return empty();
        }
        return new ShopReviewStatisticsResponse(
            true,
            result.averageTotalRating(),
            result.totalReviewCount(),
            result.recentReviewCount(),
            result.ratingCounts(),
            result.averageTasteRating(),
            result.averageAmountRating(),
            result.averagePriceRating(),
            result.averageAtmosphereRating(),
            result.averageKindnessRating(),
            result.averageHygieneRating(),
            result.willRevisitPercentage(),
            result.monthlyStats().stream()
                .map(ShopReviewMonthlyStatResponse::from)
                .toList()
        );
    }

    public static ShopReviewStatisticsResponse empty() {
        return new ShopReviewStatisticsResponse(
            false,
            null,
            null,
            null,
            Map.of(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            List.of()
        );
    }
}
