package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 가게 리뷰 통계 조회 포트(CQRS query 측 아웃바운드 포트) — 점주 통계 화면용.
 *
 * <p>점주가 조회 기간을 지정해 자기 가게의 리뷰 추이를 보는 집계를 담당한다. 회원 화면 통계는
 * {@code ReviewStatisticsQueryPort}가 소유한다.
 *
 * <p>모든 메서드가 조회 기간({@code from}~{@code to})을 받는다는 점에서 회원 화면 계약과 성격이
 * 뚜렷이 갈린다 — 이름이 같은 {@code getRatingCounts}·{@code getMonthlyReviewCounts}도 분할 전에는
 * 오버로드였을 뿐 서로 다른 메서드다.
 */
public interface ShopReviewStatisticsQueryPort {

    Double getAverageTotalRating(Long shopId, LocalDateTime from, LocalDateTime to);

    long countBetween(Long shopId, LocalDateTime from, LocalDateTime to);

    long countSince(Long shopId, LocalDateTime from);

    ShopReviewCategoryAverageResult getCategoryAverages(Long shopId, LocalDateTime from, LocalDateTime to);

    long countWillRevisitBetween(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<Integer, Long> getRatingCounts(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<String, Long> getMonthlyReviewCounts(Long shopId, LocalDateTime from, LocalDateTime to);

    Map<String, Double> getMonthlyAverageRatings(Long shopId, LocalDateTime from, LocalDateTime to);
}
