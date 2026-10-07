package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipBreakdown;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipContext;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipViewQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipBreakdownItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangeResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRegionResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipTierResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipViewResult;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;

@Service
@Transactional(readOnly = true)
class ShopDeliveryTipViewQueryService implements ShopDeliveryTipViewQueryUseCase {

    private final ShopPersistencePort shopPersistencePort;
    private final MemberDeliveryAddressPersistencePort memberDeliveryAddressPersistencePort;
    private final ShopDeliveryTipPersistencePort shopDeliveryTipPersistencePort;
    private final ShopDeliveryTipQueryPort shopDeliveryTipQueryPort;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final PublicHolidayCalendar publicHolidayCalendar;

    public ShopDeliveryTipViewQueryService(
        ShopPersistencePort shopPersistencePort,
        MemberDeliveryAddressPersistencePort memberDeliveryAddressPersistencePort,
        ShopDeliveryTipPersistencePort shopDeliveryTipPersistencePort,
        ShopDeliveryTipQueryPort shopDeliveryTipQueryPort,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        PublicHolidayCalendar publicHolidayCalendar
    ) {
        this.shopPersistencePort = shopPersistencePort;
        this.memberDeliveryAddressPersistencePort = memberDeliveryAddressPersistencePort;
        this.shopDeliveryTipPersistencePort = shopDeliveryTipPersistencePort;
        this.shopDeliveryTipQueryPort = shopDeliveryTipQueryPort;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.publicHolidayCalendar = publicHolidayCalendar;
    }

    @Override
    public ShopDeliveryTipViewResult getShopDeliveryTip(
        Long shopId,
        Long memberId,
        Long deliveryAddressId,
        Integer orderAmount,
        String orderMethod
    ) {
        Shop shop = findVisibleShopAggregate(shopId);

        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryPort.findTipRange(shopId);
        ShopDeliveryTipSettingResult setting = shopDeliveryTipQueryPort.findSetting(shopId).orElse(null);

        List<ShopDeliveryTipTierResult> tiers = shopDeliveryTipQueryPort.findTiers(shopId);
        List<ShopDeliveryTipRegionResult> regions = shopDeliveryTipQueryPort.findRegionTips(shopId);
        List<ShopDeliveryTipScheduleItemResult> schedules = shopDeliveryTipQueryPort.findScheduleTips(shopId).stream()
            .map(this::toShopDeliveryTipScheduleItemResult)
            .toList();

        ShopDeliveryTipBreakdown breakdown = calculateDeliveryTip(
            shop, memberId, deliveryAddressId, orderAmount, orderMethod
        );

        return new ShopDeliveryTipViewResult(
            breakdown == null ? null : breakdown.totalTipAmount(),
            tipRange.minDeliveryTip(),
            tipRange.maxDeliveryTip(),
            toShopDeliveryTipBreakdownItems(breakdown, orderAmount, setting, tiers),
            tiers,
            setting == null ? DeliveryTipExtraType.NONE.name() : setting.extraTipType(),
            toDistanceSetting(setting),
            regions,
            schedules,
            shopDeliveryTipQueryPort.findHolidayTipAmount(shopId)
        );
    }

    private ShopDeliveryTipBreakdown calculateDeliveryTip(
        Shop shop,
        Long memberId,
        Long deliveryAddressId,
        Integer orderAmount,
        String orderMethod
    ) {
        if (memberId == null || deliveryAddressId == null || orderAmount == null) {
            return null;
        }

        MemberDeliveryAddress deliveryAddress = memberDeliveryAddressPersistencePort.findById(deliveryAddressId)
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.MEMBER_DELIVERY_ADDRESS_NOT_FOUND));
        if (!deliveryAddress.isOwnedBy(MemberId.of(memberId))) {
            throw new ApplicationException(WebErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }

        LocalDateTime now = LocalDateTime.now();
        ShopId typedShopId = ShopId.of(shop.getId());

        return shopDeliveryTipCalculator.calculate(ShopDeliveryTipContext.of(
            OrderMethod.from(orderMethod),
            orderAmount,
            deliveryDistanceMeters(shop, deliveryAddress),
            deliveryAddress.getAdminDongId(),
            now,
            publicHolidayCalendar.isPublicHoliday(now.toLocalDate()),
            shopDeliveryTipPersistencePort.findSettingByShopId(typedShopId).orElse(null),
            shopDeliveryTipPersistencePort.findTiersByShopId(typedShopId),
            shopDeliveryTipPersistencePort.findRegionTipsByShopId(typedShopId),
            shopDeliveryTipPersistencePort.findScheduleTipsByShopId(typedShopId),
            shopDeliveryTipPersistencePort.findHolidayTipByShopId(typedShopId).orElse(null)
        ));
    }

    private Double deliveryDistanceMeters(Shop shop, MemberDeliveryAddress deliveryAddress) {
        if (shop.getLatitude() == null || shop.getLongitude() == null
            || deliveryAddress.getLatitude() == null || deliveryAddress.getLongitude() == null) {
            return null;
        }
        return GeoDistance.distanceMeters(
            shop.getLatitude(),
            shop.getLongitude(),
            deliveryAddress.getLatitude(),
            deliveryAddress.getLongitude()
        );
    }

    private List<ShopDeliveryTipBreakdownItemResult> toShopDeliveryTipBreakdownItems(
        ShopDeliveryTipBreakdown breakdown,
        Integer orderAmount,
        ShopDeliveryTipSettingResult setting,
        List<ShopDeliveryTipTierResult> tiers
    ) {
        if (breakdown == null) {
            return List.of();
        }

        List<ShopDeliveryTipBreakdownItemResult> items = new ArrayList<>();
        addBreakdownItem(items, baseTipLabel(orderAmount, tiers), breakdown.baseTipAmount());
        addBreakdownItem(items, distanceTipLabel(setting), breakdown.distanceTipAmount());
        addBreakdownItem(items, "지역별 추가", breakdown.regionTipAmount());
        addBreakdownItem(items, "시간대 할증", breakdown.scheduleTipAmount());
        addBreakdownItem(items, "공휴일 할증", breakdown.holidayTipAmount());
        return List.copyOf(items);
    }

    private void addBreakdownItem(List<ShopDeliveryTipBreakdownItemResult> items, String label, int amount) {
        if (amount > 0) {
            items.add(new ShopDeliveryTipBreakdownItemResult(label, amount));
        }
    }

    private String baseTipLabel(Integer orderAmount, List<ShopDeliveryTipTierResult> tiers) {
        if (orderAmount == null || tiers == null || tiers.isEmpty()) {
            return "기본 배달팁";
        }

        return tiers.stream()
            .filter(tier -> orderAmount >= tier.minOrderAmount())
            .max(Comparator.comparingInt(ShopDeliveryTipTierResult::minOrderAmount))
            .or(() -> tiers.stream().min(Comparator.comparingInt(ShopDeliveryTipTierResult::minOrderAmount)))
            .map(tier -> "주문금액 " + formatAmount(tier.minOrderAmount()) + "원 이상")
            .orElse("기본 배달팁");
    }

    private String distanceTipLabel(ShopDeliveryTipSettingResult setting) {
        if (setting == null || setting.baseDistanceMeters() == null) {
            return "거리 할증";
        }
        return formatKilometers(setting.baseDistanceMeters()) + "km 초과 거리 할증";
    }

    private String formatAmount(int amount) {
        return String.format(Locale.KOREA, "%,d", amount);
    }

    private String formatKilometers(int meters) {
        return meters % 1000 == 0
            ? String.valueOf(meters / 1000)
            : String.format(Locale.KOREA, "%.1f", meters / 1000.0);
    }

    private ShopDeliveryTipScheduleItemResult toShopDeliveryTipScheduleItemResult(ShopDeliveryTipScheduleResult dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        DayType dayType = DayType.from(dto.dayType());

        return new ShopDeliveryTipScheduleItemResult(
            dayType.name(),
            dayType.getDescription(),
            dto.startTime() != null ? dto.startTime().format(formatter) : null,
            dto.endTime() != null ? dto.endTime().format(formatter) : null,
            dto.tipAmount()
        );
    }

    private ShopDeliveryTipSettingResult toDistanceSetting(ShopDeliveryTipSettingResult dto) {
        if (dto == null
            || !DeliveryTipExtraType.DISTANCE.name().equals(dto.extraTipType())
            || dto.baseDistanceMeters() == null
            || dto.surchargeUnit() == null
            || dto.surchargeAmount() == null) {
            return null;
        }
        return dto;
    }

    private Shop findVisibleShopAggregate(Long shopId) {
        return shopPersistencePort.findVisibleById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
    }
}
