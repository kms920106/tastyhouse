package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopDeliveryTipCommandUseCase {

    void updateTiers(ShopDeliveryTipTiersUpdateCommand command);

    void updateDistanceTip(ShopDeliveryTipDistanceUpdateCommand command);

    void removeDistanceTip(ShopDeliveryTipDistanceRemoveCommand command);

    void updateRegionTips(ShopDeliveryTipRegionsUpdateCommand command);

    void removeRegionTips(ShopDeliveryTipRegionsRemoveCommand command);

    void updateScheduleTips(ShopDeliveryTipSchedulesUpdateCommand command);

    void updateHolidayTip(ShopDeliveryTipHolidayUpdateCommand command);
}
