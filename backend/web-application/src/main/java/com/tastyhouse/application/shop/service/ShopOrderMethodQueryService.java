package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusResult;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodItemResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

@Service
@Transactional(readOnly = true)
class ShopOrderMethodQueryService implements ShopOrderMethodQueryUseCase {

    private final ShopVisibleReader shopVisibleReader;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOperatingStatusService shopOperatingStatusService;

    public ShopOrderMethodQueryService(
        ShopVisibleReader shopVisibleReader,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopOperatingStatusService shopOperatingStatusService
    ) {
        this.shopVisibleReader = shopVisibleReader;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOperatingStatusService = shopOperatingStatusService;
    }

    @Override
    public List<ShopOrderMethodItemResult> getShopOrderMethods(Long shopId) {
        shopVisibleReader.findVisibleShop(shopId);

        Map<OrderMethod, ShopOperatingStatusResult> availabilities =
            shopOperatingStatusService.findOrderMethodAvailabilities(shopId, LocalDateTime.now());

        return ShopCodeDescriptions.ofOrderMethods(shopBasicInfoQueryPort.findOrderMethods(shopId)).stream()
            .map(dto -> toShopOrderMethodItemResult(dto, availabilities.get(OrderMethod.valueOf(dto.orderMethod()))))
            .toList();
    }

    private ShopOrderMethodItemResult toShopOrderMethodItemResult(
        ShopOrderMethodResult dto,
        ShopOperatingStatusResult availability
    ) {
        OrderUnavailableReason reason = availability == null ? null : availability.unavailableReason();
        return new ShopOrderMethodItemResult(
            dto.orderMethod(),
            dto.orderMethodDisplayName(),
            availability != null && availability.isOpen(),
            reason == null ? null : reason.name(),
            reason == null ? null : reason.getDisplayName()
        );
    }
}
