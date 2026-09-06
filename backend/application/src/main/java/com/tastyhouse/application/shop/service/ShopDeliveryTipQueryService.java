package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopDeliveryTipQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRegionResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipTierResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipOwnerViewResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopDeliveryTipQueryService implements ShopDeliveryTipQueryUseCase {

    private final ShopDeliveryTipQueryPort shopDeliveryTipQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipQueryService(ShopDeliveryTipQueryPort shopDeliveryTipQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipQueryPort = shopDeliveryTipQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopDeliveryTipOwnerViewResult getDeliveryTips(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopDeliveryTipSettingResult setting = shopDeliveryTipQueryPort.findSetting(shopId).orElse(null);

        List<ShopDeliveryTipTierResult> tiers = shopDeliveryTipQueryPort.findTiers(shopId);
        List<ShopDeliveryTipRegionResult> regions = shopDeliveryTipQueryPort.findRegionTips(shopId);
        List<ShopDeliveryTipScheduleResult> schedules = shopDeliveryTipQueryPort.findScheduleTips(shopId);

        return new ShopDeliveryTipOwnerViewResult(
            setting,
            tiers,
            regions,
            schedules,
            shopDeliveryTipQueryPort.findHolidayTipAmount(shopId)
        );
    }
}
