package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * review 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code ReviewStatisticsQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
 */
public interface ReviewStatisticsQueryPort {

    Long countVisibleByShopId(Long shopId);

    Long countWillRevisit(Long shopId);

    Double getAverageTasteRating(Long shopId);

    Double getAverageAmountRating(Long shopId);

    Double getAveragePriceRating(Long shopId);

    Double getAverageAtmosphereRating(Long shopId);

    Double getAverageKindnessRating(Long shopId);

    Double getAverageHygieneRating(Long shopId);

    Map<Integer, Long> getRatingCounts(Long shopId);

    Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year);

    Double getAverageTotalRating(Long shopId, LocalDateTime from, LocalDateTime to);

    long countBetween(Long shopId, LocalDateTime from, LocalDateTime to);

    long countSince(Long shopId, LocalDateTime from);

    ShopReviewCategoryAverageResult getCategoryAverages(Long shopId, LocalDateTime from, LocalDateTime to);

    long countWillRevisitBetween(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<Integer, Long> getRatingCounts(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<String, Long> getMonthlyReviewCounts(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<String, Double> getMonthlyAverageRatings(Long shopId, LocalDateTime from, LocalDateTime to);

    Long countVisibleByProductId(Long productId);

    Double getAverageTasteRatingByProductId(Long productId);

    Double getAverageAmountRatingByProductId(Long productId);

    Double getAveragePriceRatingByProductId(Long productId);

    long countVisibleReviewsByMemberId(Long memberId);
}
