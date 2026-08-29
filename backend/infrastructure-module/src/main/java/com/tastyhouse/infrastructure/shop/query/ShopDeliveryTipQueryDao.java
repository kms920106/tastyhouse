package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangeResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRegionResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipTierResult;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;

import static com.tastyhouse.infrastructure.region.persistence.QAdminDongJpaEntity.adminDongJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryTipHolidayJpaEntity.shopDeliveryTipHolidayJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryTipRegionJpaEntity.shopDeliveryTipRegionJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryTipScheduleJpaEntity.shopDeliveryTipScheduleJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryTipSettingJpaEntity.shopDeliveryTipSettingJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopDeliveryTipTierJpaEntity.shopDeliveryTipTierJpaEntity;

/**
 * 가게 배달팁 read 어댑터(CQRS query 측).
 *
 * <p>점주 설정 화면과 고객 배달팁 팝업이 쓰는 표현용 조회를 담당한다 — write 포트
 * {@code ShopDeliveryTipRepository}는 불변식 검증·주문 접수 산출에 필요한 조회만 갖고, 화면용 조인
 * 투영(지역 이름 조립 등)은 여기가 소유한다(CQRS 교차 주입 금지).
 *
 * <p>배달팁 5종을 한 번에 조회하는 단일 메서드를 두지 않고 파트별로 나눈 것은, 고객 팝업이 구간·설정만
 * 필요로 하는 등 소비 지점마다 필요한 파트가 다르기 때문이다. 소비 Service가 필요한 것만 조합한다.
 */
@Repository
public class ShopDeliveryTipQueryDao implements ShopDeliveryTipQueryPort {

    /**
     * 배달팁 <b>상한</b> 표기에서 가정하는 최대 배달 거리(m).
     *
     * <p>기본배달거리 허용값의 최댓값(3km)의 곱절 남짓인 5km를 잡았다 — 기본배달거리를 3km로 잡은
     * 가게도 상한이 0이 되지 않으면서, 100m당 300원짜리 최악 설정에서도 상한이 도메인 상한
     * ({@link DeliveryTipPolicy#EXTRA_TIP_UPPER_BOUND})에 닿아 그 이상 과장되지 않는다.
     *
     * <p>도메인 정책이 아니라 <b>표기용 가정</b>이라 DAO에 잔류한다({@code MAP_MARKER_RADIUS_METERS}와
     * 같은 성격) — "가게가 어디까지 배달해야 하는가"를 정하는 값이 아니라 "주소가 확정되기 전 상한을
     * 어느 거리로 근사해 보여줄 것인가"를 정한다. 가게별 배달 반경이 데이터로 생기면 사라진다.
     */
    private static final double MAX_DELIVERY_DISTANCE_METERS = 5000.0;

    private final JPAQueryFactory queryFactory;

    public ShopDeliveryTipQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /** 배달팁 설정 헤더. 설정한 적이 없는 가게는 빈 Optional이다. */
    @Override
    public Optional<ShopDeliveryTipSettingResult> findSetting(Long shopId) {
        return Optional.ofNullable(queryFactory
            .select(Projections.constructor(ShopDeliveryTipSettingResult.class,
                shopDeliveryTipSettingJpaEntity.id,
                shopDeliveryTipSettingJpaEntity.extraTipType.stringValue(),
                shopDeliveryTipSettingJpaEntity.baseDistanceMeters,
                shopDeliveryTipSettingJpaEntity.surchargeUnit.stringValue(),
                shopDeliveryTipSettingJpaEntity.surchargeAmount
            ))
            .from(shopDeliveryTipSettingJpaEntity)
            .where(shopDeliveryTipSettingJpaEntity.shopId.eq(shopId))
            .fetchFirst());
    }

    /** 구간별 배달팁 목록. 구간 순서(= 주문금액 오름차순)로 내려준다. */
    @Override
    public List<ShopDeliveryTipTierResult> findTiers(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopDeliveryTipTierResult.class,
                shopDeliveryTipTierJpaEntity.id,
                shopDeliveryTipTierJpaEntity.tierOrder,
                shopDeliveryTipTierJpaEntity.minOrderAmount,
                shopDeliveryTipTierJpaEntity.tipAmount
            ))
            .from(shopDeliveryTipTierJpaEntity)
            .where(shopDeliveryTipTierJpaEntity.shopId.eq(shopId))
            .orderBy(shopDeliveryTipTierJpaEntity.tierOrder.asc())
            .fetch();
    }

    /** 지역별 배달팁 목록 — 행정동 마스터를 조인해 표시용 이름까지 완성한다. */
    @Override
    public List<ShopDeliveryTipRegionResult> findRegionTips(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopDeliveryTipRegionResult.class,
                shopDeliveryTipRegionJpaEntity.id,
                shopDeliveryTipRegionJpaEntity.adminDongId,
                regionName(),
                shopDeliveryTipRegionJpaEntity.tipAmount
            ))
            .from(shopDeliveryTipRegionJpaEntity)
            .join(adminDongJpaEntity).on(shopDeliveryTipRegionJpaEntity.adminDongId.eq(adminDongJpaEntity.id))
            .where(shopDeliveryTipRegionJpaEntity.shopId.eq(shopId))
            .orderBy(shopDeliveryTipRegionJpaEntity.id.asc())
            .fetch();
    }

    /** 시간별 배달팁 목록. */
    @Override
    public List<ShopDeliveryTipScheduleResult> findScheduleTips(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopDeliveryTipScheduleResult.class,
                shopDeliveryTipScheduleJpaEntity.id,
                shopDeliveryTipScheduleJpaEntity.dayType.stringValue(),
                shopDeliveryTipScheduleJpaEntity.startTime,
                shopDeliveryTipScheduleJpaEntity.endTime,
                shopDeliveryTipScheduleJpaEntity.tipAmount
            ))
            .from(shopDeliveryTipScheduleJpaEntity)
            .where(shopDeliveryTipScheduleJpaEntity.shopId.eq(shopId))
            .orderBy(shopDeliveryTipScheduleJpaEntity.dayType.asc(), shopDeliveryTipScheduleJpaEntity.startTime.asc())
            .fetch();
    }

    /**
     * 공휴일 배달팁 금액. 설정하지 않았으면 0이다 — 미설정과 0원을 구분하지 않는 것이 이 팁의 규격이다
     * (0원 저장은 삭제로 해석된다).
     */
    @Override
    public int findHolidayTipAmount(Long shopId) {
        Integer tipAmount = queryFactory
            .select(shopDeliveryTipHolidayJpaEntity.tipAmount)
            .from(shopDeliveryTipHolidayJpaEntity)
            .where(shopDeliveryTipHolidayJpaEntity.shopId.eq(shopId))
            .fetchFirst();
        return tipAmount == null ? 0 : tipAmount;
    }

    /**
     * 여러 가게의 배달팁 <b>하한/상한</b>을 한 번에 산출한다 — 목록·카드·상세·팝업이 공유하는
     * <b>단일 산출 규칙</b>이며, 이 메서드가 그 규칙의 유일한 소유자다.
     *
     * <p><b>현재 시각·고객 주소에 의존하지 않는다.</b> 목록은 정렬·캐시 대상이라 요청마다 값이 달라지면
     * 안 되므로, "지금 이 주문에 붙는 금액"이 아니라 <b>설정값 전체가 만들 수 있는 하한/상한</b>을 낸다.
     *
     * <ul>
     *   <li>{@code min} = 구간별 팁 최솟값. <b>추가 배달팁은 어느 것도 하한에 넣지 않는다</b> — 넷 다
     *       "항상 붙는 것이 아니기" 때문이다. 거리별은 기본배달거리 이내면 할증이 없고, 시간별·공휴일은
     *       해당 시간대·날짜에만 붙으며, <b>지역별도 배달지 행정동이 등록된 행과 정확히 일치할 때만</b>
     *       붙는다(주소의 행정동 매칭이 실패해 {@code admin_dong_id}가 null이거나 등록되지 않은 동이면
     *       {@code ShopDeliveryTipCalculator}가 0을 낸다). 지역별을 하한에 더하면 실제로 달성 가능한
     *       금액보다 높은 "최소 ○○원"을 광고하게 되는데, 표시 가격은 실제보다 <b>낮게</b> 틀리는 편이
     *       안전하지 높게 틀리면 안 된다.</li>
     *   <li>{@code max} = 구간별 팁 최댓값 + max(거리별 상한, 지역별 팁 최댓값) + 시간별 팁 최댓값
     *       + 공휴일 팁. 거리별과 지역별은 상호 배타라 둘 중 하나만 0이 아니므로 {@code max}로 합친다.</li>
     *   <li>배달팁을 설정하지 않은 가게는 둘 다 0이다.</li>
     * </ul>
     *
     * <p><b>N+1을 만들지 않는다</b> — 목록 행마다 조회하지 않고 {@code shopId} 목록으로 배달팁 4종을
     * group-by 집계 4회(+헤더 1회)만 수행한 뒤 Java에서 합친다. 거리별 상한이 SQL 집계로 표현되지 않는
     * 올림 계산({@link #distanceUpperBound})이라 스칼라 서브쿼리 한 방으로는 낼 수 없고, 쿼리 수가
     * 가게 수와 무관하게 상수라 목록 규모가 커져도 비용이 늘지 않는다.
     */
    @Override
    public Map<Long, ShopDeliveryTipRangeResult> findTipRanges(List<Long> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, ShopDeliveryTipSettingResult> settings = findSettings(shopIds);

        Map<Long, Integer> minTierTips = new HashMap<>();
        Map<Long, Integer> maxTierTips = new HashMap<>();
        collectAmounts(queryFactory
            .select(
                shopDeliveryTipTierJpaEntity.shopId,
                shopDeliveryTipTierJpaEntity.tipAmount.min(),
                shopDeliveryTipTierJpaEntity.tipAmount.max()
            )
            .from(shopDeliveryTipTierJpaEntity)
            .where(shopDeliveryTipTierJpaEntity.shopId.in(shopIds))
            .groupBy(shopDeliveryTipTierJpaEntity.shopId)
            .fetch(), minTierTips, maxTierTips);

        // 지역별은 max에만 쓴다(하한에 넣지 않는 이유는 위 Javadoc 참고). min 맵은 collectAmounts가
        // min/max를 함께 채우는 공용 헬퍼라 받기만 하고 버린다 — 집계 쿼리 비용은 동일하다.
        Map<Long, Integer> unusedMinRegionTips = new HashMap<>();
        Map<Long, Integer> maxRegionTips = new HashMap<>();
        collectAmounts(queryFactory
            .select(
                shopDeliveryTipRegionJpaEntity.shopId,
                shopDeliveryTipRegionJpaEntity.tipAmount.min(),
                shopDeliveryTipRegionJpaEntity.tipAmount.max()
            )
            .from(shopDeliveryTipRegionJpaEntity)
            .where(shopDeliveryTipRegionJpaEntity.shopId.in(shopIds))
            .groupBy(shopDeliveryTipRegionJpaEntity.shopId)
            .fetch(), unusedMinRegionTips, maxRegionTips);

        Map<Long, Integer> maxScheduleTips = new HashMap<>();
        collectAmounts(queryFactory
            .select(
                shopDeliveryTipScheduleJpaEntity.shopId,
                shopDeliveryTipScheduleJpaEntity.tipAmount.max(),
                shopDeliveryTipScheduleJpaEntity.tipAmount.max()
            )
            .from(shopDeliveryTipScheduleJpaEntity)
            .where(shopDeliveryTipScheduleJpaEntity.shopId.in(shopIds))
            .groupBy(shopDeliveryTipScheduleJpaEntity.shopId)
            .fetch(), new HashMap<>(), maxScheduleTips);

        Map<Long, Integer> holidayTips = new HashMap<>();
        collectAmounts(queryFactory
            .select(
                shopDeliveryTipHolidayJpaEntity.shopId,
                shopDeliveryTipHolidayJpaEntity.tipAmount.max(),
                shopDeliveryTipHolidayJpaEntity.tipAmount.max()
            )
            .from(shopDeliveryTipHolidayJpaEntity)
            .where(shopDeliveryTipHolidayJpaEntity.shopId.in(shopIds))
            .groupBy(shopDeliveryTipHolidayJpaEntity.shopId)
            .fetch(), new HashMap<>(), holidayTips);

        Map<Long, ShopDeliveryTipRangeResult> ranges = new LinkedHashMap<>();
        for (Long shopId : shopIds) {
            ShopDeliveryTipSettingResult setting = settings.get(shopId);
            int minTierTip = minTierTips.getOrDefault(shopId, 0);
            int maxTierTip = maxTierTips.getOrDefault(shopId, 0);
            int maxRegionTip = usesRegion(setting) ? maxRegionTips.getOrDefault(shopId, 0) : 0;
            int maxExtraByLocation = Math.max(distanceUpperBound(setting), maxRegionTip);

            // 추가 배달팁 4종은 전부 조건부라 하한에 넣지 않는다 — 위 Javadoc 참고.
            int maxDeliveryTip = maxTierTip
                + maxExtraByLocation
                + maxScheduleTips.getOrDefault(shopId, 0)
                + holidayTips.getOrDefault(shopId, 0);

            ranges.put(shopId, new ShopDeliveryTipRangeResult(shopId, minTierTip, maxDeliveryTip));
        }
        return ranges;
    }

    /** 가게 한 곳의 배달팁 하한/상한. 설정이 없으면 0/0이다({@link #findTipRanges}와 같은 규칙). */
    @Override
    public ShopDeliveryTipRangeResult findTipRange(Long shopId) {
        return findTipRanges(List.of(shopId)).getOrDefault(shopId, ShopDeliveryTipRangeResult.none(shopId));
    }

    /**
     * 거리별 추가 배달팁이 만들 수 있는 <b>상한</b>.
     *
     * <p>거리별 할증은 원리상 거리에 비례해 무한히 커질 수 있어 그대로는 상한이 정의되지 않는다.
     * 그렇다고 {@link DeliveryTipPolicy#EXTRA_TIP_UPPER_BOUND}(10,000원)를 그대로 쓰면, 500m당 100원짜리
     * 가게도 목록에 "최대 10,000원"으로 표기돼 <b>거의 모든 가게가 같은 과장된 상한</b>을 보이게 된다.
     *
     * <p>그래서 <b>현실적인 최대 배달 거리 {@link #MAX_DELIVERY_DISTANCE_METERS}까지 배달한다고 가정</b>해
     * 산출하고, 결과는 도메인이 이미 정한 추가 배달팁 상한으로 자른다 — 자르는 계산이
     * {@code ShopDeliveryTipSetting#calculateDistanceSurcharge}와 같은 식이라 확정 계산 결과가 이 상한을
     * 넘지 않는다.
     */
    private int distanceUpperBound(ShopDeliveryTipSettingResult setting) {
        if (setting == null
            || !DeliveryTipExtraType.DISTANCE.name().equals(setting.extraTipType())
            || setting.baseDistanceMeters() == null
            || setting.surchargeUnit() == null
            || setting.surchargeAmount() == null) {
            return 0;
        }

        double excessMeters = MAX_DELIVERY_DISTANCE_METERS - (double) setting.baseDistanceMeters();
        if (excessMeters <= 0) {
            return 0;
        }

        int unitMeters = DeliveryTipDistanceUnit.from(setting.surchargeUnit()).getUnitMeters();
        int units = (int) Math.ceil(excessMeters / unitMeters);
        return Math.min(units * setting.surchargeAmount(), DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND);
    }

    private boolean usesRegion(ShopDeliveryTipSettingResult setting) {
        return setting != null && DeliveryTipExtraType.REGION.name().equals(setting.extraTipType());
    }

    /** 여러 가게의 설정 헤더를 한 번에 읽는다(목록용). 설정 행이 없는 가게는 맵에 없다. */
    private Map<Long, ShopDeliveryTipSettingResult> findSettings(List<Long> shopIds) {
        return queryFactory
            .select(
                shopDeliveryTipSettingJpaEntity.shopId,
                Projections.constructor(ShopDeliveryTipSettingResult.class,
                    shopDeliveryTipSettingJpaEntity.id,
                    shopDeliveryTipSettingJpaEntity.extraTipType.stringValue(),
                    shopDeliveryTipSettingJpaEntity.baseDistanceMeters,
                    shopDeliveryTipSettingJpaEntity.surchargeUnit.stringValue(),
                    shopDeliveryTipSettingJpaEntity.surchargeAmount
                )
            )
            .from(shopDeliveryTipSettingJpaEntity)
            .where(shopDeliveryTipSettingJpaEntity.shopId.in(shopIds))
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(shopDeliveryTipSettingJpaEntity.shopId)),
                tuple -> Objects.requireNonNull(tuple.get(1, ShopDeliveryTipSettingResult.class))
            ));
    }

    /**
     * {@code (shopId, min, max)} 3열 집계 결과를 두 맵에 나눠 담는다.
     *
     * <p>튜플을 위치로 읽는 것은 집계 대상 테이블이 4종이라 표현식 객체를 키로 넘기면 호출부마다
     * 같은 표현식을 두 번 써야 하기 때문이다 — 열 순서는 바로 위 {@code select(...)}에 고정돼 있다.
     */
    private void collectAmounts(List<Tuple> rows, Map<Long, Integer> minAmounts, Map<Long, Integer> maxAmounts) {
        for (Tuple row : rows) {
            Long shopId = Objects.requireNonNull(row.get(0, Long.class));
            Integer minAmount = row.get(1, Integer.class);
            Integer maxAmount = row.get(2, Integer.class);
            minAmounts.put(shopId, minAmount == null ? 0 : minAmount);
            maxAmounts.put(shopId, maxAmount == null ? 0 : maxAmount);
        }
    }

    /**
     * 표시용 행정동 전체 이름({@code "서울특별시 강남구 역삼1동"})을 SQL에서 조립한다 —
     * {@code ShopDeliveryAreaQueryDao}와 같은 형태여야 두 화면의 지역 표기가 갈리지 않는다.
     */
    private StringExpression regionName() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
