package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusAggregates;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethodAvailability;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureLoadPort;

@Service
public class ShopOperatingStatusService {

    private static final boolean PUBLIC_HOLIDAY = false;

    private final ShopLoadPort shopLoadPort;
    private final ShopBusinessHourLoadPort shopBusinessHourLoadPort;
    private final ShopOrderMethodLoadPort shopOrderMethodLoadPort;
    private final ShopTemporaryClosureLoadPort shopTemporaryClosureLoadPort;
    private final ShopSuspensionLoadPort shopSuspensionLoadPort;
    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopOperatingStatusService(
        ShopLoadPort shopLoadPort,
        ShopBusinessHourLoadPort shopBusinessHourLoadPort,
        ShopOrderMethodLoadPort shopOrderMethodLoadPort,
        ShopTemporaryClosureLoadPort shopTemporaryClosureLoadPort,
        ShopSuspensionLoadPort shopSuspensionLoadPort,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        this.shopLoadPort = shopLoadPort;
        this.shopBusinessHourLoadPort = shopBusinessHourLoadPort;
        this.shopOrderMethodLoadPort = shopOrderMethodLoadPort;
        this.shopTemporaryClosureLoadPort = shopTemporaryClosureLoadPort;
        this.shopSuspensionLoadPort = shopSuspensionLoadPort;
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    public ShopOperatingStatusResult findOrderAvailability(Long shopId, LocalDateTime now) {
        return findOrderAvailability(shopId, null, now);
    }

    public ShopOperatingStatusResult findOrderAvailability(Long shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopLoadPort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
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
        Shop shop = shopLoadPort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));

        ShopOperatingStatusAggregates aggregates = loadAggregates(shopId);

        Map<OrderMethod, ShopOperatingStatusResult> availabilities = new LinkedHashMap<>();
        for (ShopOrderMethod assigned : shopOrderMethodLoadPort.findOrderMethodsByShopId(shopId)) {
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

                shopId -> shopLoadPort.findById(ShopId.of(shopId))
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
            shopBusinessHourLoadPort.findBusinessHoursByShopId(shopId),
            shopBusinessHourLoadPort.findBreakTimesByShopId(shopId),
            shopBusinessHourLoadPort.findClosedDaysByShopId(shopId),
            shopTemporaryClosureLoadPort.findByShopId(shopId),
            shopSuspensionLoadPort.findByShopId(shopId)
        );
    }
}
