package com.tastyhouse.application.shop.port.out.write;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryTipSavePort {

    ShopDeliveryTipSetting saveSetting(ShopDeliveryTipSetting setting);

    List<ShopDeliveryTipTier> saveTiers(List<ShopDeliveryTipTier> tiers);

    void deleteTiersByShopId(ShopId shopId);

    List<ShopDeliveryTipRegion> saveRegionTips(List<ShopDeliveryTipRegion> regionTips);

    void deleteRegionTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipSchedule> saveScheduleTips(List<ShopDeliveryTipSchedule> scheduleTips);

    void deleteScheduleTipsByShopId(ShopId shopId);

    ShopDeliveryTipHoliday saveHolidayTip(ShopDeliveryTipHoliday holidayTip);

    void deleteHolidayTipByShopId(ShopId shopId);
}
