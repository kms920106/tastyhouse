package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryTipService {
    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryTipService(
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public List<ShopDeliveryTipTier> replaceTiers(
        ShopId shopId,
        List<ShopDeliveryTipTierSpec> specs,
        ShopChangeActor actor
    ) {
        if (specs == null || specs.isEmpty() || specs.size() > DeliveryTipPolicy.TIER_MAX_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED,
                ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED.getDefaultMessage()
                    + " 입력 구간 수: " + (specs == null ? 0 : specs.size()));
        }

        List<ShopDeliveryTipTierSpec> sorted = specs.stream()
            .sorted(Comparator.comparingInt(ShopDeliveryTipTierSpec::minOrderAmount))
            .toList();

        validateTierMonotonicity(sorted);

        String previousValue = describeTiers(shopDeliveryTipRepository.findTiersByShopId(shopId));

        shopDeliveryTipRepository.deleteTiersByShopId(shopId);

        List<ShopDeliveryTipTier> tiers = new ArrayList<>(sorted.size());
        for (int tierOrder = 0; tierOrder < sorted.size(); tierOrder++) {
            ShopDeliveryTipTierSpec spec = sorted.get(tierOrder);
            tiers.add(ShopDeliveryTipTier.of(shopId, tierOrder, spec.minOrderAmount(), spec.tipAmount()));
        }
        List<ShopDeliveryTipTier> saved = shopDeliveryTipRepository.saveTiers(tiers);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_TIER,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeTiers(saved)
        );
        return saved;
    }

    public ShopDeliveryTipSetting changeDistanceTip(
        ShopId shopId,
        int baseDistanceMeters,
        DeliveryTipDistanceUnit unit,
        int surchargeAmount,
        ShopChangeActor actor
    ) {
        if (shopDeliveryTipRepository.countRegionTipsByShopId(shopId) > 0) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT.getDefaultMessage()
                    + " 지역별 배달팁을 모두 삭제한 뒤 거리별을 설정하세요.");
        }

        ShopDeliveryTipSetting setting = loadOrCreateSetting(shopId);
        String previousValue = describeDistanceTip(setting);

        setting.changeToDistance(baseDistanceMeters, unit, surchargeAmount);
        ShopDeliveryTipSetting saved = shopDeliveryTipRepository.saveSetting(setting);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_DISTANCE,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeDistanceTip(saved)
        );
        return saved;
    }

    public void clearDistanceTip(ShopId shopId, ShopChangeActor actor) {
        shopDeliveryTipRepository.findSettingByShopId(shopId).ifPresent(setting -> {
            String previousValue = describeDistanceTip(setting);

            setting.clearExtraTip();
            shopDeliveryTipRepository.saveSetting(setting);

            shopChangeHistoryRecorder.record(
                shopId,
                ShopChangeType.DELIVERY_TIP_DISTANCE,
                ShopChangeActionType.DELETE,
                actor,
                previousValue,
                null
            );
        });
    }

    public List<ShopDeliveryTipRegion> replaceRegionTips(
        ShopId shopId,
        List<ShopDeliveryTipRegionSpec> specs,
        ShopChangeActor actor
    ) {
        List<ShopDeliveryTipRegionSpec> requested = specs == null ? List.of() : specs;

        ShopDeliveryTipSetting setting = loadOrCreateSetting(shopId);
        if (setting.usesDistance() && !requested.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_TYPE_CONFLICT.getDefaultMessage()
                    + " 거리별 배달팁을 해제한 뒤 지역별을 설정하세요.");
        }

        List<ShopDeliveryTipRegion> regionTips = buildRegionTips(shopId, requested);

        String previousValue = describeRegionTips(shopDeliveryTipRepository.findRegionTipsByShopId(shopId));

        shopDeliveryTipRepository.deleteRegionTipsByShopId(shopId);
        List<ShopDeliveryTipRegion> saved = shopDeliveryTipRepository.saveRegionTips(regionTips);

        if (requested.isEmpty()) {
            if (!setting.usesDistance()) {
                setting.clearExtraTip();
            }
        } else {
            setting.changeToRegion();
        }
        shopDeliveryTipRepository.saveSetting(setting);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_REGION,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeRegionTips(saved)
        );

        return saved;
    }

    public void clearRegionTips(ShopId shopId, ShopChangeActor actor) {
        replaceRegionTips(shopId, List.of(), actor);
    }

    public List<ShopDeliveryTipSchedule> replaceScheduleTips(
        ShopId shopId,
        List<ShopDeliveryTipScheduleSpec> specs,
        ShopChangeActor actor
    ) {
        List<ShopDeliveryTipScheduleSpec> requested = specs == null ? List.of() : specs;

        validateScheduleOverlap(requested);

        List<ShopDeliveryTipSchedule> scheduleTips = requested.stream()
            .map(spec -> ShopDeliveryTipSchedule.of(
                shopId, spec.dayType(), spec.startTime(), spec.endTime(), spec.tipAmount()
            ))
            .toList();

        String previousValue = describeScheduleTips(shopDeliveryTipRepository.findScheduleTipsByShopId(shopId));

        shopDeliveryTipRepository.deleteScheduleTipsByShopId(shopId);
        List<ShopDeliveryTipSchedule> saved = shopDeliveryTipRepository.saveScheduleTips(scheduleTips);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_SCHEDULE,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeScheduleTips(saved)
        );
        return saved;
    }

    public ShopDeliveryTipHoliday changeHolidayTip(ShopId shopId, int tipAmount, ShopChangeActor actor) {
        String previousValue = describeHolidayTip(
            shopDeliveryTipRepository.findHolidayTipByShopId(shopId).orElse(null)
        );

        if (tipAmount == 0) {
            shopDeliveryTipRepository.deleteHolidayTipByShopId(shopId);

            shopChangeHistoryRecorder.record(
                shopId,
                ShopChangeType.DELIVERY_TIP_HOLIDAY,
                ShopChangeActionType.UPDATE,
                actor,
                previousValue,
                describeHolidayTip(null)
            );
            return null;
        }

        ShopDeliveryTipHoliday holidayTip = shopDeliveryTipRepository.findHolidayTipByShopId(shopId)
            .map(existing -> {
                existing.changeTipAmount(tipAmount);
                return existing;
            })
            .orElseGet(() -> ShopDeliveryTipHoliday.of(shopId, tipAmount));

        ShopDeliveryTipHoliday saved = shopDeliveryTipRepository.saveHolidayTip(holidayTip);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_TIP_HOLIDAY,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeHolidayTip(saved)
        );
        return saved;
    }

    private ShopDeliveryTipSetting loadOrCreateSetting(ShopId shopId) {
        return shopDeliveryTipRepository.findSettingByShopId(shopId)
            .orElseGet(() -> ShopDeliveryTipSetting.of(shopId));
    }

    private String describeTiers(List<ShopDeliveryTipTier> tiers) {
        return ShopChangeValueFormatter.snapshot(
            tiers.stream()
                .sorted(Comparator.comparingInt(ShopDeliveryTipTier::getTierOrder))
                .map(tier -> ShopChangeValueFormatter.amount(tier.getMinOrderAmount()) + " 이상: "
                    + ShopChangeValueFormatter.amount(tier.getTipAmount()))
                .toList()
        );
    }

    private String describeDistanceTip(ShopDeliveryTipSetting setting) {
        if (setting == null || !setting.usesDistance()
            || setting.getBaseDistanceMeters() == null
            || setting.getSurchargeUnit() == null
            || setting.getSurchargeAmount() == null) {
            return ShopChangeValueFormatter.unset();
        }
        return ShopChangeValueFormatter.distanceKm(toKilometers(setting.getBaseDistanceMeters())) + "까지: "
            + setting.getSurchargeUnit().getUnitMeters() + "m당 "
            + ShopChangeValueFormatter.amount(setting.getSurchargeAmount());
    }

    private String describeRegionTips(List<ShopDeliveryTipRegion> regionTips) {
        Map<Long, String> namesById = adminDongRepository
            .findAllByIds(regionTips.stream().map(ShopDeliveryTipRegion::getAdminDongId).toList())
            .stream()
            .collect(Collectors.toMap(AdminDong::getId, AdminDong::getDongName, (first, second) -> first));

        return ShopChangeValueFormatter.snapshot(
            regionTips.stream()
                .map(regionTip -> {
                    Long adminDongId = regionTip.getAdminDongId().value();
                    String name = namesById.getOrDefault(adminDongId, "행정동 " + adminDongId);
                    return name + ": +" + ShopChangeValueFormatter.amount(regionTip.getTipAmount());
                })
                .toList()
        );
    }

    private String describeScheduleTips(List<ShopDeliveryTipSchedule> scheduleTips) {
        return ShopChangeValueFormatter.snapshot(
            scheduleTips.stream()
                .map(scheduleTip -> scheduleTip.getDayType().getDescription() + " "
                    + ShopChangeValueFormatter.timeRange(scheduleTip.getStartTime(), scheduleTip.getEndTime())
                    + ": +" + ShopChangeValueFormatter.amount(scheduleTip.getTipAmount()))
                .toList()
        );
    }

    private String describeHolidayTip(ShopDeliveryTipHoliday holidayTip) {
        if (holidayTip == null) {
            return ShopChangeValueFormatter.unset();
        }
        return "공휴일: +" + ShopChangeValueFormatter.amount(holidayTip.getTipAmount());
    }

    private BigDecimal toKilometers(int meters) {
        return BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }

    private void validateTierMonotonicity(List<ShopDeliveryTipTierSpec> sorted) {
        for (int i = 1; i < sorted.size(); i++) {
            ShopDeliveryTipTierSpec previous = sorted.get(i - 1);
            ShopDeliveryTipTierSpec current = sorted.get(i);

            if (current.minOrderAmount() <= previous.minOrderAmount()) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_ASCENDING,
                    ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_ASCENDING.getDefaultMessage()
                        + " 중복 금액: " + current.minOrderAmount() + "원");
            }
            if (current.tipAmount() >= previous.tipAmount()) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_DESCENDING,
                    ErrorCode.SHOP_DELIVERY_TIP_TIER_NOT_DESCENDING.getDefaultMessage()
                        + " " + previous.minOrderAmount() + "원 구간 팁: " + previous.tipAmount()
                        + "원, " + current.minOrderAmount() + "원 구간 팁: " + current.tipAmount() + "원");
            }
        }
    }

    private List<ShopDeliveryTipRegion> buildRegionTips(ShopId shopId, List<ShopDeliveryTipRegionSpec> specs) {
        Set<Long> seenAdminDongIds = new HashSet<>();
        List<ShopDeliveryTipRegion> regionTips = new ArrayList<>(specs.size());

        for (ShopDeliveryTipRegionSpec spec : specs) {
            if (!seenAdminDongIds.add(spec.adminDongId())) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_REGION_DUPLICATED,
                    ErrorCode.SHOP_DELIVERY_TIP_REGION_DUPLICATED.getDefaultMessage()
                        + " 행정동 ID: " + spec.adminDongId());
            }

            AdminDongId adminDongId = AdminDongId.of(spec.adminDongId());
            if (!adminDongRepository.existsById(adminDongId)) {
                throw new BusinessException(ErrorCode.ADMIN_DONG_NOT_FOUND,
                    ErrorCode.ADMIN_DONG_NOT_FOUND.getDefaultMessage() + " 행정동 ID: " + spec.adminDongId());
            }
            if (!shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
                throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_REGION_NOT_IN_DELIVERY_AREA,
                    ErrorCode.SHOP_DELIVERY_TIP_REGION_NOT_IN_DELIVERY_AREA.getDefaultMessage()
                        + " 행정동 ID: " + spec.adminDongId());
            }

            regionTips.add(ShopDeliveryTipRegion.of(shopId, adminDongId, spec.tipAmount()));
        }
        return regionTips;
    }

    private void validateScheduleOverlap(List<ShopDeliveryTipScheduleSpec> specs) {
        for (int i = 0; i < specs.size(); i++) {
            for (int j = i + 1; j < specs.size(); j++) {
                ShopDeliveryTipScheduleSpec left = specs.get(i);
                ShopDeliveryTipScheduleSpec right = specs.get(j);

                if (left.dayType() != right.dayType()) {
                    continue;
                }
                if (overlaps(left, right)) {
                    throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP,
                        ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP.getDefaultMessage()
                            + " " + left.dayType() + " " + left.startTime() + "~" + left.endTime()
                            + " / " + right.startTime() + "~" + right.endTime());
                }
            }
        }
    }

    private boolean overlaps(ShopDeliveryTipScheduleSpec left, ShopDeliveryTipScheduleSpec right) {
        for (int[] leftSegment : toSegments(left.startTime(), left.endTime())) {
            for (int[] rightSegment : toSegments(right.startTime(), right.endTime())) {
                if (leftSegment[0] < rightSegment[1] && rightSegment[0] < leftSegment[1]) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<int[]> toSegments(LocalTime startTime, LocalTime endTime) {
        int start = toMinuteOfDay(startTime);
        int end = toMinuteOfDay(endTime);
        int endOfDay = 24 * 60;

        if (end <= start) {
            return List.of(new int[] {start, endOfDay}, new int[] {0, end});
        }
        return List.of(new int[] {start, end});
    }

    private int toMinuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
