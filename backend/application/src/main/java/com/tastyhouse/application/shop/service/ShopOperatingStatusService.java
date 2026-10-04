package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusAggregates;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethodAvailability;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosurePersistencePort;

@Service
public class ShopOperatingStatusService {

    private static final boolean PUBLIC_HOLIDAY = false;

    private final ShopPersistencePort shopPersistencePort;
    private final ShopDetailPersistencePort shopDetailPersistencePort;
    private final ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort;
    private final ShopSuspensionPersistencePort shopSuspensionPersistencePort;
    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopOperatingStatusService(
        ShopPersistencePort shopPersistencePort,
        ShopDetailPersistencePort shopDetailPersistencePort,
        ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort,
        ShopSuspensionPersistencePort shopSuspensionPersistencePort,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        this.shopPersistencePort = shopPersistencePort;
        this.shopDetailPersistencePort = shopDetailPersistencePort;
        this.shopTemporaryClosurePersistencePort = shopTemporaryClosurePersistencePort;
        this.shopSuspensionPersistencePort = shopSuspensionPersistencePort;
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    public ShopOperatingStatusResult findOrderAvailability(Long shopId, LocalDateTime now) {
        return findOrderAvailability(shopId, null, now);
    }

    public ShopOperatingStatusResult findOrderAvailability(Long shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopPersistencePort.findById(ShopId.of(shopId))
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
        Shop shop = shopPersistencePort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        ShopOperatingStatusAggregates aggregates = loadAggregates(shopId);

        Map<OrderMethod, ShopOperatingStatusResult> availabilities = new LinkedHashMap<>();
        for (ShopOrderMethod assigned : shopDetailPersistencePort.findOrderMethodsByShopId(shopId)) {
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

                shopId -> shopPersistencePort.findById(ShopId.of(shopId))
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
            shopDetailPersistencePort.findBusinessHoursByShopId(shopId),
            shopDetailPersistencePort.findBreakTimesByShopId(shopId),
            shopDetailPersistencePort.findClosedDaysByShopId(shopId),
            shopTemporaryClosurePersistencePort.findByShopId(shopId),
            shopSuspensionPersistencePort.findByShopId(shopId)
        );
    }
}
