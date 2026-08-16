package com.tastyhouse.ceoapi.review;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.infrastructure.review.query.ReviewBlindRequestHistoryResult;
import com.tastyhouse.infrastructure.review.query.ReviewStatisticsQueryDao;
import com.tastyhouse.infrastructure.review.query.ShopReviewCategoryAverageResult;
import com.tastyhouse.infrastructure.review.query.ShopReviewDisplaySettingQueryDao;
import com.tastyhouse.infrastructure.review.query.ShopReviewManagementDetailResult;
import com.tastyhouse.infrastructure.review.query.ShopReviewManagementListItemResult;
import com.tastyhouse.infrastructure.review.query.ShopReviewManagementQueryDao;
import com.tastyhouse.infrastructure.review.query.ShopReviewManagementSearchCondition;
import com.tastyhouse.infrastructure.review.query.ShopReviewSortTypeResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.review.response.ReviewBlindReasonCatalogResponse;
import com.tastyhouse.ceoapi.review.response.ReviewBlindRequestHistoryResponse;
import com.tastyhouse.ceoapi.review.response.ShopReviewDetailResponse;
import com.tastyhouse.ceoapi.review.response.ShopReviewListItemResponse;
import com.tastyhouse.ceoapi.review.response.ShopReviewMonthlyStatResponse;
import com.tastyhouse.ceoapi.review.response.ShopReviewSortTypeResponse;
import com.tastyhouse.ceoapi.review.response.ShopReviewStatisticsResponse;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주 리뷰 관리 조회 서비스(CQRS query 측).
 *
 * <p>모든 가게 스코프 조회는 {@link ShopOwnershipValidator}를 <b>가장 먼저</b> 호출한다 — 생략하면 남의
 * 가게 리뷰가 통째로 새는 IDOR이 된다. 하위 리소스({@code reviewId})는 경로의 {@code shopId}만 믿을 수
 * 없으므로 조회 결과의 {@code shopId}와 대조해 재검증한다.
 *
 * <p>{@code startDate}/{@code endDate}의 상·하한 관계는 Bean Validation이 아니라 이 서비스가 판정한다 —
 * 두 필드에 걸친 하나의 규칙이라 어노테이션으로 쪼개면 같은 규칙 위반인데 응답 계약이 갈린다.
 */
@Service
@Transactional(readOnly = true)
public class ShopReviewQueryService {

    /** 원문 ②의 "리뷰 고유 번호 16자리" 표시 폭. */
    private static final int REVIEW_NUMBER_LENGTH = 16;

    /** 통계 집계 기간(최근 6개월). */
    private static final int STATISTICS_MONTHS = 6;

    /** 대시보드 노출 게이트 기간 — 이 기간에 리뷰가 1건도 없으면 통계를 내리지 않는다(원문 규격). */
    private static final int DASHBOARD_GATE_DAYS = 180;

    /** 원문 "최근 리뷰수"의 기준 기간. */
    private static final int RECENT_REVIEW_DAYS = 30;

    private final ShopReviewManagementQueryDao shopReviewManagementQueryDao;
    private final ReviewStatisticsQueryDao reviewStatisticsQueryDao;
    private final ShopReviewDisplaySettingQueryDao shopReviewDisplaySettingQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewQueryService(
        ShopReviewManagementQueryDao shopReviewManagementQueryDao,
        ReviewStatisticsQueryDao reviewStatisticsQueryDao,
        ShopReviewDisplaySettingQueryDao shopReviewDisplaySettingQueryDao,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewManagementQueryDao = shopReviewManagementQueryDao;
        this.reviewStatisticsQueryDao = reviewStatisticsQueryDao;
        this.shopReviewDisplaySettingQueryDao = shopReviewDisplaySettingQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 내 가게 리뷰 목록을 페이징 조회한다.
     *
     * <p>{@code sortType}이 생략되면 저장된 기본 정렬을, 그것도 없으면 최신순을 적용한다.
     */
    public PaginationResponse<ShopReviewListItemResponse> getReviews(
        Long ceoId,
        Long shopId,
        String tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        String sortType,
        int page,
        int size
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateDateRange(startDate, endDate);

        ReviewListTab tabFilter = tab == null ? ReviewListTab.ALL : ReviewListTab.from(tab);
        OrderMethod orderMethodFilter = orderMethod == null ? null : OrderMethod.from(orderMethod);

        ShopReviewManagementSearchCondition condition = ShopReviewManagementSearchCondition.of(
            shopId,
            tabFilter,
            startDate,
            endDate,
            rating,
            orderMethodFilter,
            hasImage,
            resolveSortType(shopId, sortType)
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<ShopReviewListItemResponse> pageResult =
            shopReviewManagementQueryDao.findShopReviews(condition, pageQuery)
                .map(this::toListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 리뷰 상세를 조회한다.
     *
     * <p>리뷰가 이 가게 것이 아니면 {@code SHOP_ACCESS_DENIED}(403)다 — 리뷰는 web에 공개된 리소스라
     * 존재 자체가 비밀이 아니므로 404로 숨기지 않는다.
     */
    public ShopReviewDetailResponse getReviewDetail(Long ceoId, Long shopId, Long reviewId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopReviewManagementDetailResult detail =
            shopReviewManagementQueryDao.findShopReviewDetail(ReviewId.of(reviewId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!shopId.equals(detail.shopId())) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }

        return toDetailResponse(detail);
    }

    /**
     * 리뷰 통계 대시보드를 조회한다.
     *
     * <p><b>당일 포함 최근 180일 리뷰가 1건도 없으면 전체 대시보드를 노출하지 않는다</b>(원문 규격) —
     * 게이트를 먼저 판정해 통과하지 못하면 나머지 집계 쿼리를 아예 실행하지 않는다.
     */
    public ShopReviewStatisticsResponse getStatistics(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gateFrom = now.minusDays(DASHBOARD_GATE_DAYS);
        if (reviewStatisticsQueryDao.countSince(shopId, gateFrom) == 0) {
            return ShopReviewStatisticsResponse.empty();
        }

        YearMonth currentMonth = YearMonth.from(now);
        YearMonth firstMonth = currentMonth.minusMonths(STATISTICS_MONTHS - 1L);
        LocalDateTime periodFrom = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime periodTo = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        long totalReviewCount = reviewStatisticsQueryDao.countBetween(shopId, periodFrom, periodTo);
        long willRevisitCount =
            reviewStatisticsQueryDao.countWillRevisitBetween(shopId, periodFrom, periodTo);
        ShopReviewCategoryAverageResult averages =
            reviewStatisticsQueryDao.getCategoryAverages(shopId, periodFrom, periodTo);

        return ShopReviewStatisticsResponse.from(
            true,
            roundToTenth(reviewStatisticsQueryDao.getAverageTotalRating(shopId, periodFrom, periodTo)),
            totalReviewCount,
            reviewStatisticsQueryDao.countSince(shopId, now.minusDays(RECENT_REVIEW_DAYS)),
            normalizeRatingCounts(reviewStatisticsQueryDao.getRatingCounts(shopId, periodFrom, periodTo)),
            roundToTenth(averages.tasteRating()),
            roundToTenth(averages.amountRating()),
            roundToTenth(averages.priceRating()),
            roundToTenth(averages.atmosphereRating()),
            roundToTenth(averages.kindnessRating()),
            roundToTenth(averages.hygieneRating()),
            toPercentage(willRevisitCount, totalReviewCount),
            toMonthlyStats(shopId, firstMonth, periodFrom, periodTo)
        );
    }

    /**
     * 저장된 리뷰 정렬 설정을 조회한다. 미설정 가게는 기본값 {@code LATEST}에 {@code updatedAt = null}이다.
     */
    public ShopReviewSortTypeResponse getSortType(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopReviewDisplaySettingQueryDao.findSortTypeSettingByShopId(shopId)
            .map(this::toSortTypeResponse)
            .orElseGet(() -> toSortTypeResponse(new ShopReviewSortTypeResult(ReviewSortType.LATEST, null)));
    }

    /**
     * 게시중단 요청 사유 카탈로그. 가게에 종속되지 않는 정적 목록이라 소유권 검증이 없다.
     */
    public List<ReviewBlindReasonCatalogResponse> getBlindReasons() {
        return Arrays.stream(ReviewBlindReason.values())
            .map(reason -> ReviewBlindReasonCatalogResponse.from(reason.name(), reason.getDescription()))
            .toList();
    }

    /**
     * 최근 6개월 월별 통계를 <b>정확히 6칸</b>으로 만든다.
     *
     * <p>DAO는 리뷰가 있는 달만 돌려주므로 빈 달을 여기서 채운다 — 그래프가 6칸 고정이어야 화면이 축을
     * 다시 계산하지 않는다. 리뷰 0건인 달의 평점은 {@code null}이다(0.0으로 채우면 별점 0점으로 읽힌다).
     */
    private List<ShopReviewMonthlyStatResponse> toMonthlyStats(
        Long shopId,
        YearMonth firstMonth,
        LocalDateTime periodFrom,
        LocalDateTime periodTo
    ) {
        Map<String, Long> counts = reviewStatisticsQueryDao.getMonthlyReviewCounts(shopId, periodFrom, periodTo);
        Map<String, Double> averages =
            reviewStatisticsQueryDao.getMonthlyAverageRatings(shopId, periodFrom, periodTo);

        List<ShopReviewMonthlyStatResponse> monthlyStats = new java.util.ArrayList<>(STATISTICS_MONTHS);
        for (int offset = 0; offset < STATISTICS_MONTHS; offset++) {
            YearMonth month = firstMonth.plusMonths(offset);
            String key = month.toString();
            monthlyStats.add(ShopReviewMonthlyStatResponse.from(
                key,
                roundToTenth(averages.get(key)),
                counts.getOrDefault(key, 0L)
            ));
        }
        return monthlyStats;
    }

    /**
     * 별점별 리뷰 수의 키 1~5를 항상 채운다. 0건인 별점이 응답에서 빠지면 화면이 키 존재 여부를 분기해야 한다.
     */
    private Map<Integer, Long> normalizeRatingCounts(Map<Integer, Long> ratingCounts) {
        Map<Integer, Long> normalized = new LinkedHashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            normalized.put(rating, ratingCounts.getOrDefault(rating, 0L));
        }
        return normalized;
    }

    /**
     * 재방문 의사 비율(%). 모집단이 0이면 {@code null}이다 — 0%로 응답하면 "아무도 재방문하지 않는다"로 읽힌다.
     */
    private Double toPercentage(long count, long total) {
        if (total == 0) {
            return null;
        }
        return roundToTenth((double) count * 100 / total);
    }

    /**
     * 소수 1자리로 반올림. 집계가 없으면(리뷰 0건) {@code null}을 그대로 통과시킨다.
     */
    private Double roundToTenth(Double value) {
        return value == null ? null : Math.round(value * 10) / 10.0;
    }

    /**
     * 적용할 정렬 — 명시값 &gt; 저장 설정 &gt; {@code LATEST}.
     */
    private ReviewSortType resolveSortType(Long shopId, String sortType) {
        if (sortType != null) {
            return ReviewSortType.from(sortType);
        }
        return shopReviewDisplaySettingQueryDao.findSortTypeByShopId(shopId)
            .orElse(ReviewSortType.LATEST);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.REVIEW_DATE_RANGE_INVALID);
        }
    }

    /**
     * 리뷰 ID를 16자리 0-pad 표시용 번호로 만든다(원문 ②).
     */
    private String toReviewNumber(Long reviewId) {
        return String.format("%0" + REVIEW_NUMBER_LENGTH + "d", reviewId);
    }

    private ShopReviewListItemResponse toListItemResponse(ShopReviewManagementListItemResult result) {
        OrderMethod orderMethod = result.orderMethod();
        ApprovalStatus blindRequestStatus = result.blindRequestStatus();
        return ShopReviewListItemResponse.from(
            result.id(),
            toReviewNumber(result.id()),
            result.memberNickname(),
            result.totalRating(),
            result.content(),
            result.imageUrls(),
            result.productNames(),
            orderMethod == null ? null : orderMethod.name(),
            orderMethod == null ? null : orderMethod.getDisplayName(),
            result.hidden(),
            result.ownerOnly(),
            result.ownerReplyContent(),
            result.ownerReplyCreatedAt(),
            blindRequestStatus == null ? null : blindRequestStatus.name(),
            result.createdAt(),
            toReplyDeadline(result.createdAt()),
            isReplyable(result.createdAt())
        );
    }

    private ShopReviewDetailResponse toDetailResponse(ShopReviewManagementDetailResult result) {
        OrderMethod orderMethod = result.orderMethod();
        List<ReviewBlindRequestHistoryResponse> blindRequests = result.blindRequests().stream()
            .map(this::toBlindRequestHistoryResponse)
            .toList();
        return ShopReviewDetailResponse.from(
            result.id(),
            toReviewNumber(result.id()),
            result.memberNickname(),
            result.totalRating(),
            result.content(),
            result.imageUrls(),
            result.productNames(),
            orderMethod == null ? null : orderMethod.name(),
            orderMethod == null ? null : orderMethod.getDisplayName(),
            result.hidden(),
            result.ownerOnly(),
            result.tasteRating(),
            result.amountRating(),
            result.priceRating(),
            result.atmosphereRating(),
            result.kindnessRating(),
            result.hygieneRating(),
            result.willRevisit(),
            result.tagNames(),
            result.ownerReplyId(),
            result.ownerReplyContent(),
            result.ownerReplyCreatedAt(),
            result.ownerReplyUpdatedAt(),
            latestBlindRequestStatus(blindRequests),
            blindRequests,
            result.createdAt(),
            toReplyDeadline(result.createdAt()),
            isReplyable(result.createdAt()),
            result.deliveryRating(),
            result.deliveryComment()
        );
    }

    /**
     * 답변 마감일 = 리뷰 작성일 + {@link ReviewOwnerReply#REPLY_PERIOD_DAYS}일.
     *
     * <p><b>DB 컬럼으로 두지 않는다</b> — {@code REVIEW.created_at}에서 매번 파생되는 값이라 저장하면
     * 정책(일수)이 바뀌는 순간 기존 행이 전부 틀린 값을 갖게 된다.
     */
    private LocalDate toReplyDeadline(LocalDateTime reviewCreatedAt) {
        return reviewCreatedAt.toLocalDate().plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);
    }

    /**
     * 오늘 기준 <b>신규 등록</b> 가능 여부 — 점주가 400을 받고 나서야 마감을 아는 것을 막기 위한 파생값이다.
     *
     * <p>판정 기준은 도메인 서비스({@code ReviewOwnerReplyService})의 기한 검증과 동일한 날짜 경계다.
     * 이미 답변이 있는 리뷰는 이 값과 무관하게 수정·삭제할 수 있다(수정·삭제에는 기한 제한이 없다).
     */
    private boolean isReplyable(LocalDateTime reviewCreatedAt) {
        return !LocalDate.now().isAfter(toReplyDeadline(reviewCreatedAt));
    }

    /**
     * 이력이 최신순으로 정렬돼 있으므로 첫 항목이 최근 상태다. 목록 응답의 {@code blindRequestStatus}와
     * 같은 값을 상세에서도 내려주기 위한 것이다(같은 화면이 두 응답을 번갈아 쓴다).
     */
    private String latestBlindRequestStatus(List<ReviewBlindRequestHistoryResponse> blindRequests) {
        return blindRequests.isEmpty() ? null : blindRequests.getFirst().status();
    }

    private ReviewBlindRequestHistoryResponse toBlindRequestHistoryResponse(ReviewBlindRequestHistoryResult result) {
        ReviewBlindReason reason = result.reason();
        ApprovalStatus status = result.status();
        return ReviewBlindRequestHistoryResponse.from(
            result.id(),
            reason.name(),
            reason.getDescription(),
            result.detailReason(),
            status.name(),
            status.getDescription(),
            result.rejectReason(),
            result.createdAt()
        );
    }

    private ShopReviewSortTypeResponse toSortTypeResponse(ShopReviewSortTypeResult result) {
        ReviewSortType sortType = result.sortType();
        return ShopReviewSortTypeResponse.from(
            sortType.name(),
            describeSortType(sortType),
            result.updatedAt()
        );
    }

    /**
     * 정렬 방식 한글명.
     *
     * <p>{@link ReviewSortType}에 {@code description}을 넣지 않고 여기서 붙이는 이유는, 그 enum이 web·ceo
     * 공용인데 표시 문구는 화면 소관이기 때문이다. {@code valueOf}가 아니라 switch로 써서 상수가 추가되면
     * 컴파일이 깨져 문구 누락이 드러나게 한다.
     */
    private String describeSortType(ReviewSortType sortType) {
        return switch (sortType) {
            case RECOMMENDED -> "추천순";
            case LATEST -> "최신순";
            case OLDEST -> "등록순";
        };
    }
}
