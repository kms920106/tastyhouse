package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

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

/**
 * 배달팁 5종 write 어댑터.
 *
 * <p>{@link ShopDeliveryTipRepository}와 함께 {@link ShopDeliveryTipRegionLookup}도 구현한다 —
 * 두 포트가 같은 테이블({@code SHOP_DELIVERY_TIP_REGION})을 읽으므로 어댑터를 쪼개면 같은 쿼리가 두 곳에
 * 생긴다. 포트를 나눈 것은 소비자({@code ShopDeliveryAreaService})의 의존을 좁히기 위함이지 저장소를
 * 나누기 위함이 아니다.
 *
 * <p>저장 시맨틱은 <b>load-copy-save</b>다 — detached {@code save()}(merge)는
 * {@code @CreatedDate(updatable = false)} 감사 필드를 깨뜨린다. 컬렉션 3종(구간·지역별·시간별)은
 * replace-all 교체라 개별 update 경로가 없어 항상 insert다.
 */
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
}
