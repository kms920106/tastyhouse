package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopDeliveryTipHolidayJpaEntity.shopDeliveryTipHolidayJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopDeliveryTipRegionJpaEntity.shopDeliveryTipRegionJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopDeliveryTipScheduleJpaEntity.shopDeliveryTipScheduleJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopDeliveryTipSettingJpaEntity.shopDeliveryTipSettingJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopDeliveryTipTierJpaEntity.shopDeliveryTipTierJpaEntity;

@Repository
class ShopDeliveryTipPersistenceAdapter implements ShopDeliveryTipLoadPort, ShopDeliveryTipSavePort, ShopDeliveryTipRegionLoadPort {

    private final JPAQueryFactory queryFactory;
    private final ShopDeliveryTipSettingJpaRepository shopDeliveryTipSettingJpaRepository;
    private final ShopDeliveryTipTierJpaRepository shopDeliveryTipTierJpaRepository;
    private final ShopDeliveryTipRegionJpaRepository shopDeliveryTipRegionJpaRepository;
    private final ShopDeliveryTipScheduleJpaRepository shopDeliveryTipScheduleJpaRepository;
    private final ShopDeliveryTipHolidayJpaRepository shopDeliveryTipHolidayJpaRepository;
    private final EntityManager entityManager;

    public ShopDeliveryTipPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopDeliveryTipSettingJpaRepository shopDeliveryTipSettingJpaRepository,
        ShopDeliveryTipTierJpaRepository shopDeliveryTipTierJpaRepository,
        ShopDeliveryTipRegionJpaRepository shopDeliveryTipRegionJpaRepository,
        ShopDeliveryTipScheduleJpaRepository shopDeliveryTipScheduleJpaRepository,
        ShopDeliveryTipHolidayJpaRepository shopDeliveryTipHolidayJpaRepository,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.shopDeliveryTipSettingJpaRepository = shopDeliveryTipSettingJpaRepository;
        this.shopDeliveryTipTierJpaRepository = shopDeliveryTipTierJpaRepository;
        this.shopDeliveryTipRegionJpaRepository = shopDeliveryTipRegionJpaRepository;
        this.shopDeliveryTipScheduleJpaRepository = shopDeliveryTipScheduleJpaRepository;
        this.shopDeliveryTipHolidayJpaRepository = shopDeliveryTipHolidayJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ShopDeliveryTipSetting> findSettingByShopId(ShopId shopId) {
        ShopDeliveryTipSettingJpaEntity entity = queryFactory
            .selectFrom(shopDeliveryTipSettingJpaEntity)
            .where(shopDeliveryTipSettingJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopDeliveryTipMapper::toDomain);
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
        return queryFactory
            .selectFrom(shopDeliveryTipTierJpaEntity)
            .where(shopDeliveryTipTierJpaEntity.shopId.eq(shopId.value()))
            .orderBy(shopDeliveryTipTierJpaEntity.tierOrder.asc())
            .fetch()
            .stream()
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
        entityManager.flush();
        queryFactory
            .delete(shopDeliveryTipTierJpaEntity)
            .where(shopDeliveryTipTierJpaEntity.shopId.eq(shopId.value()))
            .execute();
        entityManager.clear();
    }

    @Override
    public List<ShopDeliveryTipRegion> findRegionTipsByShopId(ShopId shopId) {
        return queryFactory
            .selectFrom(shopDeliveryTipRegionJpaEntity)
            .where(shopDeliveryTipRegionJpaEntity.shopId.eq(shopId.value()))
            .fetch()
            .stream()
            .map(ShopDeliveryTipMapper::toDomain)
            .toList();
    }

    @Override
    public long countRegionTipsByShopId(ShopId shopId) {
        Long count = queryFactory
            .select(shopDeliveryTipRegionJpaEntity.count())
            .from(shopDeliveryTipRegionJpaEntity)
            .where(shopDeliveryTipRegionJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return count == null ? 0L : count;
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
        entityManager.flush();
        queryFactory
            .delete(shopDeliveryTipRegionJpaEntity)
            .where(shopDeliveryTipRegionJpaEntity.shopId.eq(shopId.value()))
            .execute();
        entityManager.clear();
    }

    @Override
    public List<ShopDeliveryTipSchedule> findScheduleTipsByShopId(ShopId shopId) {
        return queryFactory
            .selectFrom(shopDeliveryTipScheduleJpaEntity)
            .where(shopDeliveryTipScheduleJpaEntity.shopId.eq(shopId.value()))
            .fetch()
            .stream()
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
        List<ShopDeliveryTipScheduleJpaEntity> rows = queryFactory
            .selectFrom(shopDeliveryTipScheduleJpaEntity)
            .where(shopDeliveryTipScheduleJpaEntity.shopId.eq(shopId.value()))
            .fetch();
        shopDeliveryTipScheduleJpaRepository.deleteAll(rows);
    }

    @Override
    public Optional<ShopDeliveryTipHoliday> findHolidayTipByShopId(ShopId shopId) {
        ShopDeliveryTipHolidayJpaEntity entity = queryFactory
            .selectFrom(shopDeliveryTipHolidayJpaEntity)
            .where(shopDeliveryTipHolidayJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopDeliveryTipMapper::toDomain);
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
        entityManager.flush();
        queryFactory
            .delete(shopDeliveryTipHolidayJpaEntity)
            .where(shopDeliveryTipHolidayJpaEntity.shopId.eq(shopId.value()))
            .execute();
        entityManager.clear();
    }

    @Override
    public boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
        return queryFactory
            .selectOne()
            .from(shopDeliveryTipRegionJpaEntity)
            .where(
                shopDeliveryTipRegionJpaEntity.shopId.eq(shopId.value()),
                shopDeliveryTipRegionJpaEntity.adminDongId.eq(adminDongId.value())
            )
            .fetchFirst() != null;
    }

    @Override
    public Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId) {
        return queryFactory
            .selectFrom(shopDeliveryTipRegionJpaEntity)
            .where(shopDeliveryTipRegionJpaEntity.shopId.eq(shopId.value()))
            .fetch()
            .stream()
            .map(ShopDeliveryTipRegionJpaEntity::getAdminDongId)
            .map(AdminDongId::of)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
