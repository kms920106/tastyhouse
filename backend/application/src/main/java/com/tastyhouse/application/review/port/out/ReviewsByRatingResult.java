package com.tastyhouse.application.review.port.out;

import java.util.List;
import java.util.Map;

/**
 * 평점대별 리뷰 묶음 결과.
 *
 * <p>core 원본은 lombok {@code @Getter}/{@code @AllArgsConstructor} 클래스였으나, 같은 패키지의 다른
 * result와 형태를 맞추기 위해 record로 전환했다. 필드 구성·순서는 원본과 동일하며 접근자만 record accessor
 * ({@code reviewsByRating()} 등)로 바뀐다.
 */
public record ReviewsByRatingResult(
    Map<Integer, List<LatestReviewListItemResult>> reviewsByRating,
    List<LatestReviewListItemResult> allReviews,
    Long totalReviewCount,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
}
