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

@Repository
public class ShopDeliveryTipQueryDao implements ShopDeliveryTipQueryPort {
    private static final double MAX_DELIVERY_DISTANCE_METERS = 5000.0;

    private final JPAQueryFactory queryFactory;

    public ShopDeliveryTipQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

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

    @Override
    public int findHolidayTipAmount(Long shopId) {
        Integer tipAmount = queryFactory
            .select(shopDeliveryTipHolidayJpaEntity.tipAmount)
            .from(shopDeliveryTipHolidayJpaEntity)
            .where(shopDeliveryTipHolidayJpaEntity.shopId.eq(shopId))
            .fetchFirst();
        return tipAmount == null ? 0 : tipAmount;
    }

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

            int maxDeliveryTip = maxTierTip
                + maxExtraByLocation
                + maxScheduleTips.getOrDefault(shopId, 0)
                + holidayTips.getOrDefault(shopId, 0);

            ranges.put(shopId, new ShopDeliveryTipRangeResult(shopId, minTierTip, maxDeliveryTip));
        }
        return ranges;
    }

    @Override
    public ShopDeliveryTipRangeResult findTipRange(Long shopId) {
        return findTipRanges(List.of(shopId)).getOrDefault(shopId, ShopDeliveryTipRangeResult.none(shopId));
    }

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

    private void collectAmounts(List<Tuple> rows, Map<Long, Integer> minAmounts, Map<Long, Integer> maxAmounts) {
        for (Tuple row : rows) {
            Long shopId = Objects.requireNonNull(row.get(0, Long.class));
            Integer minAmount = row.get(1, Integer.class);
            Integer maxAmount = row.get(2, Integer.class);
            minAmounts.put(shopId, minAmount == null ? 0 : minAmount);
            maxAmounts.put(shopId, maxAmount == null ? 0 : maxAmount);
        }
    }

    private StringExpression regionName() {
        return adminDongJpaEntity.sidoName
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.sigunguName)
            .concat(Expressions.asString(" "))
            .concat(adminDongJpaEntity.dongName);
    }
}
