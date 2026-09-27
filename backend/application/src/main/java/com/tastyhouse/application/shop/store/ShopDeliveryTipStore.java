package com.tastyhouse.application.shop.store;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipStatePort;

public class ShopDeliveryTipStore implements ShopDeliveryTipRepository, ShopDeliveryTipRegionLookup {
    private final ShopDeliveryTipStatePort shopDeliveryTipStatePort;

    public ShopDeliveryTipStore(ShopDeliveryTipStatePort shopDeliveryTipStatePort) {
        this.shopDeliveryTipStatePort = shopDeliveryTipStatePort;
    }

    @Override
    public Optional<ShopDeliveryTipSetting> findSettingByShopId(ShopId shopId) {
        return shopDeliveryTipStatePort.findSettingByShopId(shopId.value()).map(ShopDeliveryTipSettingStateMapper::toDomain);
    }

    @Override
    public ShopDeliveryTipSetting saveSetting(ShopDeliveryTipSetting setting) {
        return ShopDeliveryTipSettingStateMapper.toDomain(shopDeliveryTipStatePort.saveSetting(ShopDeliveryTipSettingStateMapper.toState(setting)));
    }

    @Override
    public List<ShopDeliveryTipTier> findTiersByShopId(ShopId shopId) {
        return shopDeliveryTipStatePort.findTiersByShopId(shopId.value()).stream()
            .map(ShopDeliveryTipTierStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ShopDeliveryTipTier> saveTiers(List<ShopDeliveryTipTier> tiers) {
        return shopDeliveryTipStatePort.saveTiers(tiers.stream().map(ShopDeliveryTipTierStateMapper::toState).toList()).stream()
            .map(ShopDeliveryTipTierStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteTiersByShopId(ShopId shopId) {
        shopDeliveryTipStatePort.deleteTiersByShopId(shopId.value());
    }

    @Override
    public List<ShopDeliveryTipRegion> findRegionTipsByShopId(ShopId shopId) {
        return shopDeliveryTipStatePort.findRegionTipsByShopId(shopId.value()).stream()
            .map(ShopDeliveryTipRegionStateMapper::toDomain)
            .toList();
    }

    @Override
    public long countRegionTipsByShopId(ShopId shopId) {
        return shopDeliveryTipStatePort.countRegionTipsByShopId(shopId.value());
    }

    @Override
    public List<ShopDeliveryTipRegion> saveRegionTips(List<ShopDeliveryTipRegion> regionTips) {
        return shopDeliveryTipStatePort.saveRegionTips(regionTips.stream().map(ShopDeliveryTipRegionStateMapper::toState).toList()).stream()
            .map(ShopDeliveryTipRegionStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteRegionTipsByShopId(ShopId shopId) {
        shopDeliveryTipStatePort.deleteRegionTipsByShopId(shopId.value());
    }

    @Override
    public List<ShopDeliveryTipSchedule> findScheduleTipsByShopId(ShopId shopId) {
        return shopDeliveryTipStatePort.findScheduleTipsByShopId(shopId.value()).stream()
            .map(ShopDeliveryTipScheduleStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ShopDeliveryTipSchedule> saveScheduleTips(List<ShopDeliveryTipSchedule> scheduleTips) {
        return shopDeliveryTipStatePort.saveScheduleTips(scheduleTips.stream().map(ShopDeliveryTipScheduleStateMapper::toState).toList()).stream()
            .map(ShopDeliveryTipScheduleStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteScheduleTipsByShopId(ShopId shopId) {
        shopDeliveryTipStatePort.deleteScheduleTipsByShopId(shopId.value());
    }

    @Override
    public Optional<ShopDeliveryTipHoliday> findHolidayTipByShopId(ShopId shopId) {
        return shopDeliveryTipStatePort.findHolidayTipByShopId(shopId.value()).map(ShopDeliveryTipHolidayStateMapper::toDomain);
    }

    @Override
    public ShopDeliveryTipHoliday saveHolidayTip(ShopDeliveryTipHoliday holidayTip) {
        return ShopDeliveryTipHolidayStateMapper.toDomain(shopDeliveryTipStatePort.saveHolidayTip(ShopDeliveryTipHolidayStateMapper.toState(holidayTip)));
    }

    @Override
    public void deleteHolidayTipByShopId(ShopId shopId) {
        shopDeliveryTipStatePort.deleteHolidayTipByShopId(shopId.value());
    }

    @Override
    public boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
        return shopDeliveryTipStatePort.existsRegionTipByShopIdAndAdminDongId(shopId.value(), adminDongId.value());
    }

    @Override
    public Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId) {
        return shopDeliveryTipStatePort.findRegionTipAdminDongIds(shopId.value()).stream()
            .map(AdminDongId::of)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
