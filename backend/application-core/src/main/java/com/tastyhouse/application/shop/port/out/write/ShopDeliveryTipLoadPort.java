package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryTipLoadPort {

    Optional<ShopDeliveryTipSetting> findSettingByShopId(ShopId shopId);

    List<ShopDeliveryTipTier> findTiersByShopId(ShopId shopId);

    List<ShopDeliveryTipRegion> findRegionTipsByShopId(ShopId shopId);

    long countRegionTipsByShopId(ShopId shopId);

    List<ShopDeliveryTipSchedule> findScheduleTipsByShopId(ShopId shopId);

    Optional<ShopDeliveryTipHoliday> findHolidayTipByShopId(ShopId shopId);
}
