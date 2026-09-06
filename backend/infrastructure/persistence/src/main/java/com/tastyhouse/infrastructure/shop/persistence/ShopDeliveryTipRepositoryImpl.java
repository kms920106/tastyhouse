package com.tastyhouse.infrastructure.shop.persistence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ShopDeliveryTipRepositoryImpl implements ShopDeliveryTipRepository, ShopDeliveryTipRegionLookup {
    private final ShopDeliveryTipSettingJpaRepository shopDeliveryTipSettingJpaRepository;
    private final ShopDeliveryTipTierJpaRepository shopDeliveryTipTierJpaRepository;
    private final ShopDeliveryTipRegionJpaRepository shopDeliveryTipRegionJpaRepository;
    private final ShopDeliveryTipScheduleJpaRepository shopDeliveryTipScheduleJpaRepository;
    private final ShopDeliveryTipHolidayJpaRepository shopDeliveryTipHolidayJpaRepository;

    public ShopDeliveryTipRepositoryImpl(
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
    public Optional<ShopDeliveryTipSetting> findSettingByShopId(ShopId shopId) {
        return shopDeliveryTipSettingJpaRepository.findByShopId(shopId.value())
            .map(ShopDeliveryTipMapper::toDomain);
    }

    @Override
    public ShopDeliveryTipSetting saveSetting(ShopDeliveryTipSetting setting) {
        if (setting.getId() == null) {
            ShopDeliveryTipSettingJpaEntity saved = shopDeliveryTipSettingJpaRepository
                .save(ShopDeliveryTipMapper.toEntity(setting));
            return ShopDeliveryTipMapper.toDomain(saved);
        }

        ShopDeliveryTipSettingJpaEntity entity = shopDeliveryTipSettingJpaRepository.findById(setting.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배달팁 설정입니다: " + setting.getId()));
        ShopDeliveryTipMapper.applyChanges(entity, setting);
        return ShopDeliveryTipMapper.toDomain(entity);
    }

    @Override
    public List<ShopDeliveryTipTier> findTiersByShopId(ShopId shopId) {
        return shopDeliveryTipTierJpaRepository.findByShopIdOrderByTierOrderAsc(shopId.value()).stream()
            .map(ShopDeliveryTipMapper::toDomain)
            .toList();
    }

    @Override
    public List<ShopDeliveryTipTier> saveTiers(List<ShopDeliveryTipTier> tiers) {
        List<ShopDeliveryTipTierJpaEntity> entities = tiers.stream()
            .map(ShopDeliveryTipMapper::toEntity)
            .toList();
        return shopDeliveryTipTierJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryTipMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public void deleteTiersByShopId(ShopId shopId) {
        shopDeliveryTipTierJpaRepository.deleteByShopId(shopId.value());
    }

    @Override
    public List<ShopDeliveryTipRegion> findRegionTipsByShopId(ShopId shopId) {
        return shopDeliveryTipRegionJpaRepository.findByShopId(shopId.value()).stream()
            .map(ShopDeliveryTipMapper::toDomain)
            .toList();
    }

    @Override
    public long countRegionTipsByShopId(ShopId shopId) {
        return shopDeliveryTipRegionJpaRepository.countByShopId(shopId.value());
    }

    @Override
    public List<ShopDeliveryTipRegion> saveRegionTips(List<ShopDeliveryTipRegion> regionTips) {
        List<ShopDeliveryTipRegionJpaEntity> entities = regionTips.stream()
            .map(ShopDeliveryTipMapper::toEntity)
            .toList();
        return shopDeliveryTipRegionJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryTipMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public void deleteRegionTipsByShopId(ShopId shopId) {
        shopDeliveryTipRegionJpaRepository.deleteByShopId(shopId.value());
    }

    @Override
    public List<ShopDeliveryTipSchedule> findScheduleTipsByShopId(ShopId shopId) {
        return shopDeliveryTipScheduleJpaRepository.findByShopId(shopId.value()).stream()
            .map(ShopDeliveryTipMapper::toDomain)
            .toList();
    }

    @Override
    public List<ShopDeliveryTipSchedule> saveScheduleTips(List<ShopDeliveryTipSchedule> scheduleTips) {
        List<ShopDeliveryTipScheduleJpaEntity> entities = scheduleTips.stream()
            .map(ShopDeliveryTipMapper::toEntity)
            .toList();
        return shopDeliveryTipScheduleJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryTipMapper::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public void deleteScheduleTipsByShopId(ShopId shopId) {
        shopDeliveryTipScheduleJpaRepository.deleteByShopId(shopId.value());
    }

    @Override
    public Optional<ShopDeliveryTipHoliday> findHolidayTipByShopId(ShopId shopId) {
        return shopDeliveryTipHolidayJpaRepository.findByShopId(shopId.value())
            .map(ShopDeliveryTipMapper::toDomain);
    }

    @Override
    public ShopDeliveryTipHoliday saveHolidayTip(ShopDeliveryTipHoliday holidayTip) {
        if (holidayTip.getId() == null) {
            ShopDeliveryTipHolidayJpaEntity saved = shopDeliveryTipHolidayJpaRepository
                .save(ShopDeliveryTipMapper.toEntity(holidayTip));
            return ShopDeliveryTipMapper.toDomain(saved);
        }

        ShopDeliveryTipHolidayJpaEntity entity = shopDeliveryTipHolidayJpaRepository.findById(holidayTip.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 공휴일 배달팁입니다: " + holidayTip.getId()));
        ShopDeliveryTipMapper.applyChanges(entity, holidayTip);
        return ShopDeliveryTipMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void deleteHolidayTipByShopId(ShopId shopId) {
        shopDeliveryTipHolidayJpaRepository.deleteByShopId(shopId.value());
    }

    @Override
    public boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
        return shopDeliveryTipRegionJpaRepository.existsByShopIdAndAdminDongId(shopId.value(), adminDongId.value());
    }

    @Override
    public Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId) {
        return shopDeliveryTipRegionJpaRepository.findByShopId(shopId.value()).stream()
            .map(ShopDeliveryTipRegionJpaEntity::getAdminDongId)
            .map(AdminDongId::of)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
