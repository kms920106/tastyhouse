package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.tastyhouse.domain.product.model.ProductPrice;

/**
 * 매장가격 관련 뱃지 2종의 노출 조건을 판정하는 정책(순수 계산).
 *
 * <p>리포지토리를 주입받지 않는 <b>순수 계산 정책</b>이다 — 조회는 호출부(api 모듈 QueryService)가
 * 수행하고 이 클래스는 판정만 한다. 그래야 같은 판정을 손님 화면·점주 화면·배치가 각자의 조회 경로로
 * 재사용할 수 있고, 단위 테스트가 DB 없이 성립한다({@code ProductExposureCalculator}와 같은 형태).
 *
 * <p><b>두 뱃지의 조건이 다르다</b>:
 * <ul>
 *   <li><b>매장과 같은 가격</b> — 가게의 매장가격 인증이 ON이면 노출된다. 가격 행을 보지 않는다
 *       (인증 자체가 이미 "배달가 ≤ 매장가"를 보장하며, 어긋나면 인증이 내려간다)</li>
 *   <li><b>매장가격 픽업</b> — 픽업가 ≤ 매장가 <b>이고</b> 전체 메뉴 기준 80% 이상이 매장가·픽업가를
 *       가져야 하며, <b>픽업가 설정 익일(영업일)부터</b> 노출된다</li>
 * </ul>
 */
public class StorePriceBadgePolicy {

    /** '매장가격 픽업' 뱃지의 메뉴 커버리지 하한 — 전체 메뉴의 80% 이상이 매장가·픽업가를 가져야 한다. */
    private static final double PICKUP_BADGE_COVERAGE_THRESHOLD = 0.8d;

    /**
     * '매장가격 픽업' 뱃지를 노출할지 판정한다.
     *
     * <p><b>익일(영업일) 노출 규정을 지킨다.</b> 픽업가를 설정한 당일에는 노출하지 않고, 설정 시각
     * 이후 <b>영업일이 한 번 지나간 뒤</b>부터 노출한다 — 손님이 뱃지를 보고 방문했을 때 가격이 이미
     * 적용돼 있어야 하기 때문이다. 판정 대상 가격 행 중 <b>가장 늦게 설정된 픽업가</b>를 기준으로 삼는다
     * (일부만 익일이 지났다고 뱃지를 켜면 아직 반영되지 않은 메뉴가 섞인다).
     *
     * @param prices           가게의 (삭제되지 않은) 전체 메뉴 가격 행
     * @param totalProductCount 가게의 전체 메뉴 수 — 커버리지 분모다
     * @param businessDates    설정 시각 이후 지나간 영업일 목록(호출부가 영업시간·휴무로 산출)
     * @param now              판정 시각
     */
    public boolean shouldExposePickupBadge(
        List<ProductPrice> prices,
        long totalProductCount,
        List<LocalDate> businessDates,
        LocalDateTime now
    ) {
        if (prices == null || prices.isEmpty() || totalProductCount <= 0L) {
            return false;
        }

        // 픽업가가 매장가를 넘는 메뉴가 하나라도 있으면 "매장가격 픽업"이라는 표시가 거짓이 된다.
        boolean anyPickupAboveStore = prices.stream()
            .filter(ProductPrice::hasStoreAndPickupPrice)
            .anyMatch(price -> !price.isPickupPriceWithinStorePrice());
        if (anyPickupAboveStore) {
            return false;
        }

        if (!meetsCoverage(prices, totalProductCount)) {
            return false;
        }

        return hasBusinessDayPassed(prices, businessDates, now);
    }

    /**
     * 매장가·픽업가를 모두 가진 메뉴가 전체의 80% 이상인지 판정한다.
     *
     * <p><b>분모는 가격 행 수가 아니라 메뉴 수다</b> — 가격 행으로 세면 가격명이 여러 개인 메뉴가
     * 가중치를 더 갖게 되어, 곱빼기까지 매장가를 넣은 메뉴 몇 개로 커버리지를 채울 수 있다.
     * 한 메뉴는 가격 행 중 <b>하나라도</b> 매장가·픽업가를 가지면 충족으로 센다.
     */
    private static boolean meetsCoverage(List<ProductPrice> prices, long totalProductCount) {
        long coveredProductCount = prices.stream()
            .filter(ProductPrice::hasStoreAndPickupPrice)
            .map(price -> price.getProductId().value())
            .distinct()
            .count();
        return coveredProductCount >= Math.ceil(totalProductCount * PICKUP_BADGE_COVERAGE_THRESHOLD);
    }

    /**
     * 가장 늦게 설정된 픽업가 이후로 영업일이 한 번이라도 지났는지 판정한다.
     *
     * <p>설정 시각이 없는 행(과거 데이터)은 기준에서 제외한다 — 시각을 모르는 행 때문에 뱃지가 영구히
     * 막히면 이관 이전 데이터를 가진 가게가 이 기능을 영원히 쓸 수 없다.
     */
    private static boolean hasBusinessDayPassed(
        List<ProductPrice> prices,
        List<LocalDate> businessDates,
        LocalDateTime now
    ) {
        LocalDateTime latestSetAt = prices.stream()
            .filter(ProductPrice::hasStoreAndPickupPrice)
            .map(ProductPrice::getPickupPriceSetAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        // 설정 시각을 아는 행이 하나도 없으면 익일 규정을 적용할 근거가 없으므로 통과시킨다.
        if (latestSetAt == null) {
            return true;
        }
        if (businessDates == null || businessDates.isEmpty()) {
            return false;
        }

        LocalDate setDate = latestSetAt.toLocalDate();
        // 설정일 이후의 영업일이 하나라도 시작됐어야 한다 — 설정일 당일은 세지 않는다.
        return businessDates.stream()
            .anyMatch(date -> date.isAfter(setDate) && !date.isAfter(now.toLocalDate()));
    }

    /**
     * '매장과 같은 가격' 뱃지를 노출할지 판정한다 — 가게의 매장가격 인증 ON 여부가 유일한 조건이다.
     *
     * <p>가격 행을 다시 보지 않는 이유는 인증 상태가 이미 그 판정의 결과이기 때문이다. 가격이 바뀌어
     * 배달가가 매장가를 넘으면 저장 시점에 인증이 내려가므로({@code ProductPriceService}), 이 플래그
     * 하나가 언제나 최신 진실이다. 여기서 다시 계산하면 두 판정이 어긋날 여지만 생긴다.
     */
    public boolean shouldExposeSameAsStorePriceBadge(boolean storePriceVerified) {
        return storePriceVerified;
    }
}
