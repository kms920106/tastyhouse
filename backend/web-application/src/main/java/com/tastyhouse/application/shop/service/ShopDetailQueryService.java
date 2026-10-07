package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusResult;
import com.tastyhouse.application.shop.port.in.ShopDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangeResult;
import com.tastyhouse.application.shop.port.out.ShopDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;
import com.tastyhouse.application.shop.port.out.ShopVisibleDetailResult;

@Service
@Transactional(readOnly = true)
class ShopDetailQueryService implements ShopDetailQueryUseCase {

    private final ShopVisibleReader shopVisibleReader;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopDeliveryTipQueryPort shopDeliveryTipQueryPort;
    private final ShopOperatingStatusService shopOperatingStatusService;

    public ShopDetailQueryService(
        ShopVisibleReader shopVisibleReader,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopDeliveryTipQueryPort shopDeliveryTipQueryPort,
        ShopOperatingStatusService shopOperatingStatusService
    ) {
        this.shopVisibleReader = shopVisibleReader;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopDeliveryTipQueryPort = shopDeliveryTipQueryPort;
        this.shopOperatingStatusService = shopOperatingStatusService;
    }

    @Override
    public ShopDetailViewResult getShopDetail(Long shopId) {
        ShopVisibleDetailResult shop = shopVisibleReader.findVisibleShop(shopId);

        List<ShopPhoneNumberResult> phoneNumbers = shopBasicInfoQueryPort.findPhoneNumbers(shopId);

        String trademarkImageUrl = shopBasicInfoQueryPort.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::trademarkImageUrl)
            .orElse(null);

        ShopOperatingStatusResult operatingStatus =
            shopOperatingStatusService.findOrderAvailability(shopId, LocalDateTime.now());
        OrderUnavailableReason unavailableReason = operatingStatus.unavailableReason();

        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryPort.findTipRange(shopId);

        return new ShopDetailViewResult(
            shop.id(),
            shop.name(),
            shop.latitude(),
            shop.longitude(),
            shop.rating(),
            shop.roadAddress(),
            shop.lotAddress(),
            shop.phoneNumber(),
            phoneNumbers,
            trademarkImageUrl,
            operatingStatus.status().name(),
            unavailableReason == null ? null : unavailableReason.name(),
            unavailableReason == null ? null : unavailableReason.getDisplayName(),
            shop.minOrderAmount(),
            tipRange.minDeliveryTip(),
            tipRange.maxDeliveryTip(),
            shop.scheduledOrderEnabled()
        );
    }
}
