package com.tastyhouse.infrastructure.shop.persistence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipHolidayState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipScheduleState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipSettingState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipTierState;

@Repository
public class ShopDeliveryTipStatePortImpl implements ShopDeliveryTipStatePort {
    private final ShopDeliveryTipSettingJpaRepository shopDeliveryTipSettingJpaRepository;
    private final ShopDeliveryTipTierJpaRepository shopDeliveryTipTierJpaRepository;
    private final ShopDeliveryTipRegionJpaRepository shopDeliveryTipRegionJpaRepository;
    private final ShopDeliveryTipScheduleJpaRepository shopDeliveryTipScheduleJpaRepository;
    private final ShopDeliveryTipHolidayJpaRepository shopDeliveryTipHolidayJpaRepository;

    public ShopDeliveryTipStatePortImpl(
        ShopDeliveryTipSettingJpaRepository shopDeliveryTipSettingJpaRepository,
        ShopDeliveryTipTierJpaRepository shopDeliveryTipTierJpaRepository,
        ShopDeliveryTipRegionJpaRepository shopDeliveryTipRegionJpaRepository,
        ShopDeliveryTipScheduleJpaRepository shopDeliveryTipScheduleJpaRepository,
        ShopDeliveryTipHolidayJpaRepository shopDeliveryTipHolidayJpaRepository
    ) {
        this.shopDeliveryTipSettingJpaRepository = shopDeliveryTipSettingJpaRepository;
        this.shopDeliveryTipTierJpaRepository = shopDeliveryTipTierJpaRepository;
        this.shopDeliveryTipRegionJpaRepository = shopDeliveryTipRegionJpaRepository;
        this.shopDeliveryTipScheduleJpaRepository = shopDeliveryTipScheduleJpaRepository;
        this.shopDeliveryTipHolidayJpaRepository = shopDeliveryTipHolidayJpaRepository;
    }

    @Override
    public Optional<ShopDeliveryTipSettingState> findSettingByShopId(Long shopId) {
        return shopDeliveryTipSettingJpaRepository.findByShopId(shopId)
            .map(ShopDeliveryTipMapper::toState);
    }

    @Override
    public ShopDeliveryTipSettingState saveSetting(ShopDeliveryTipSettingState setting) {
        if (setting.id() == null) {
            ShopDeliveryTipSettingJpaEntity saved = shopDeliveryTipSettingJpaRepository
                .save(ShopDeliveryTipMapper.toEntity(setting));
            return ShopDeliveryTipMapper.toState(saved);
        }

        ShopDeliveryTipSettingJpaEntity entity = shopDeliveryTipSettingJpaRepository.findById(setting.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배달팁 설정입니다: " + setting.id()));
        ShopDeliveryTipMapper.applyChanges(entity, setting);
        return ShopDeliveryTipMapper.toState(entity);
    }

    @Override
    public List<ShopDeliveryTipTierState> findTiersByShopId(Long shopId) {
        return shopDeliveryTipTierJpaRepository.findByShopIdOrderByTierOrderAsc(shopId).stream()
            .map(ShopDeliveryTipMapper::toState)
            .toList();
    }

    @Override
    public List<ShopDeliveryTipTierState> saveTiers(List<ShopDeliveryTipTierState> tiers) {
        List<ShopDeliveryTipTierJpaEntity> entities = tiers.stream()
            .map(ShopDeliveryTipMapper::toEntity)
            .toList();
        return shopDeliveryTipTierJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryTipMapper::toState)
            .toList();
    }

    @Override
    @Transactional
    public void deleteTiersByShopId(Long shopId) {
        shopDeliveryTipTierJpaRepository.deleteByShopId(shopId);
    }

    @Override
    public List<ShopDeliveryTipRegionState> findRegionTipsByShopId(Long shopId) {
        return shopDeliveryTipRegionJpaRepository.findByShopId(shopId).stream()
            .map(ShopDeliveryTipMapper::toState)
            .toList();
    }

    @Override
    public long countRegionTipsByShopId(Long shopId) {
        return shopDeliveryTipRegionJpaRepository.countByShopId(shopId);
    }

    @Override
    public List<ShopDeliveryTipRegionState> saveRegionTips(List<ShopDeliveryTipRegionState> regionTips) {
        List<ShopDeliveryTipRegionJpaEntity> entities = regionTips.stream()
            .map(ShopDeliveryTipMapper::toEntity)
            .toList();
        return shopDeliveryTipRegionJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryTipMapper::toState)
            .toList();
    }

    @Override
    @Transactional
    public void deleteRegionTipsByShopId(Long shopId) {
        shopDeliveryTipRegionJpaRepository.deleteByShopId(shopId);
    }

    @Override
    public List<ShopDeliveryTipScheduleState> findScheduleTipsByShopId(Long shopId) {
        return shopDeliveryTipScheduleJpaRepository.findByShopId(shopId).stream()
            .map(ShopDeliveryTipMapper::toState)
            .toList();
    }

    @Override
    public List<ShopDeliveryTipScheduleState> saveScheduleTips(List<ShopDeliveryTipScheduleState> scheduleTips) {
        List<ShopDeliveryTipScheduleJpaEntity> entities = scheduleTips.stream()
            .map(ShopDeliveryTipMapper::toEntity)
            .toList();
        return shopDeliveryTipScheduleJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryTipMapper::toState)
            .toList();
    }

    @Override
    @Transactional
    public void deleteScheduleTipsByShopId(Long shopId) {
        shopDeliveryTipScheduleJpaRepository.deleteByShopId(shopId);
    }

    @Override
    public Optional<ShopDeliveryTipHolidayState> findHolidayTipByShopId(Long shopId) {
        return shopDeliveryTipHolidayJpaRepository.findByShopId(shopId)
            .map(ShopDeliveryTipMapper::toState);
    }

    @Override
    public ShopDeliveryTipHolidayState saveHolidayTip(ShopDeliveryTipHolidayState holidayTip) {
        if (holidayTip.id() == null) {
            ShopDeliveryTipHolidayJpaEntity saved = shopDeliveryTipHolidayJpaRepository
                .save(ShopDeliveryTipMapper.toEntity(holidayTip));
            return ShopDeliveryTipMapper.toState(saved);
        }

        ShopDeliveryTipHolidayJpaEntity entity = shopDeliveryTipHolidayJpaRepository.findById(holidayTip.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 공휴일 배달팁입니다: " + holidayTip.id()));
        ShopDeliveryTipMapper.applyChanges(entity, holidayTip);
        return ShopDeliveryTipMapper.toState(entity);
    }

    @Override
    @Transactional
    public void deleteHolidayTipByShopId(Long shopId) {
        shopDeliveryTipHolidayJpaRepository.deleteByShopId(shopId);
    }

    @Override
    public boolean existsRegionTipByShopIdAndAdminDongId(Long shopId, Long adminDongId) {
        return shopDeliveryTipRegionJpaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId);
    }

    @Override
    public Set<Long> findRegionTipAdminDongIds(Long shopId) {
        return shopDeliveryTipRegionJpaRepository.findByShopId(shopId).stream()
            .map(ShopDeliveryTipRegionJpaEntity::getAdminDongId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
