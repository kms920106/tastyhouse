package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.application.shop.port.out.write.ShopDetailRepository;
import com.tastyhouse.application.shop.port.out.write.ShopRepository;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionRepository;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusAggregates;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.service.ShopOrderMethodAvailability;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopOperatingStatusService {
    private static final boolean PUBLIC_HOLIDAY = false;

    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;
    private final ShopSuspensionRepository shopSuspensionRepository;
    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopOperatingStatusService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.shopTemporaryClosureRepository = shopTemporaryClosureRepository;
        this.shopSuspensionRepository = shopSuspensionRepository;
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    public ShopOperatingStatusResult findOrderAvailability(Long shopId, LocalDateTime now) {
        return findOrderAvailability(shopId, null, now);
    }

    public ShopOperatingStatusResult findOrderAvailability(Long shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        return calculate(shop, shopId, orderMethod, now);
    }

    public ShopOrderMethodAvailability findOrderAvailability(Shop shop, OrderMethod orderMethod, LocalDateTime now) {
        ShopOperatingStatusAggregates aggregates = loadAggregates(shop.getId());

        return new ShopOrderMethodAvailability(
            shopOperatingStatusCalculator.calculate(aggregates.toContext(shop, null, PUBLIC_HOLIDAY, now)),
            shopOperatingStatusCalculator.calculate(aggregates.toContext(shop, orderMethod, PUBLIC_HOLIDAY, now))
        );
    }

    public Map<OrderMethod, ShopOperatingStatusResult> findOrderMethodAvailabilities(Long shopId, LocalDateTime now) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        ShopOperatingStatusAggregates aggregates = loadAggregates(shopId);

        Map<OrderMethod, ShopOperatingStatusResult> availabilities = new LinkedHashMap<>();
        for (ShopOrderMethod assigned : shopDetailRepository.findOrderMethodsByShopId(shopId)) {
            OrderMethod orderMethod = assigned.getOrderMethod();
            availabilities.put(orderMethod, shopOperatingStatusCalculator.calculate(
                aggregates.toContext(shop, orderMethod, PUBLIC_HOLIDAY, now)
            ));
        }
        return availabilities;
    }

    public Map<Long, ShopOperatingStatus> findOperatingStatuses(List<Long> shopIds, LocalDateTime now) {
        return shopIds.stream()
            .distinct()
            .collect(Collectors.toMap(
                Function.identity(),

                shopId -> shopRepository.findById(ShopId.of(shopId))
                    .map(shop -> calculate(shop, shopId, null, now).status())
                    .orElse(ShopOperatingStatus.PREPARING)
            ));
    }

    private ShopOperatingStatusResult calculate(
        Shop shop,
        Long shopId,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        return shopOperatingStatusCalculator.calculate(
            loadAggregates(shopId).toContext(shop, orderMethod, PUBLIC_HOLIDAY, now)
        );
    }

    private ShopOperatingStatusAggregates loadAggregates(Long shopId) {
        return ShopOperatingStatusAggregates.of(
            shopDetailRepository.findBusinessHoursByShopId(shopId),
            shopDetailRepository.findBreakTimesByShopId(shopId),
            shopDetailRepository.findClosedDaysByShopId(shopId),
            shopTemporaryClosureRepository.findByShopId(shopId),
            shopSuspensionRepository.findByShopId(shopId)
        );
    }
}
