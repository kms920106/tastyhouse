package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopBreakTimeJpaEntity.shopBreakTimeJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopBusinessHourJpaEntity.shopBusinessHourJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopClosedDayJpaEntity.shopClosedDayJpaEntity;

@Repository
class ShopBusinessHourPersistenceAdapter implements ShopBusinessHourLoadPort, ShopBusinessHourSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopBusinessHourJpaRepository shopBusinessHourJpaRepository;
    private final ShopBreakTimeJpaRepository shopBreakTimeJpaRepository;
    private final ShopClosedDayJpaRepository shopClosedDayJpaRepository;

    public ShopBusinessHourPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopBusinessHourJpaRepository shopBusinessHourJpaRepository,
        ShopBreakTimeJpaRepository shopBreakTimeJpaRepository,
        ShopClosedDayJpaRepository shopClosedDayJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopBusinessHourJpaRepository = shopBusinessHourJpaRepository;
        this.shopBreakTimeJpaRepository = shopBreakTimeJpaRepository;
        this.shopClosedDayJpaRepository = shopClosedDayJpaRepository;
    }

    @Override
    public List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBusinessHourJpaEntity)
            .where(shopBusinessHourJpaEntity.shopId.eq(shopId))
            .orderBy(shopBusinessHourJpaEntity.dayType.asc())
            .fetch()
            .stream()
            .map(ShopBusinessHourMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopBusinessHour> findBusinessHourById(Long id) {
        return shopBusinessHourJpaRepository.findById(id).map(ShopBusinessHourMapper::toDomain);
    }

    @Override
    public ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour) {
        if (businessHour.getId() == null) {
            ShopBusinessHourJpaEntity saved = shopBusinessHourJpaRepository.save(ShopBusinessHourMapper.toEntity(businessHour));
            return ShopBusinessHourMapper.toDomain(saved);
        }

        ShopBusinessHourJpaEntity entity = shopBusinessHourJpaRepository.findById(businessHour.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 영업시간입니다: " + businessHour.getId()));
        ShopBusinessHourMapper.applyChanges(entity, businessHour);
        return ShopBusinessHourMapper.toDomain(entity);
    }

    @Override
    public void deleteBusinessHourById(Long id) {
        shopBusinessHourJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopBreakTime> findBreakTimesByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopBreakTimeJpaEntity)
            .where(shopBreakTimeJpaEntity.shopId.eq(shopId))
            .orderBy(shopBreakTimeJpaEntity.dayType.asc())
            .fetch()
            .stream()
            .map(ShopBreakTimeMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopBreakTime> findBreakTimeById(Long id) {
        return shopBreakTimeJpaRepository.findById(id).map(ShopBreakTimeMapper::toDomain);
    }

    @Override
    public ShopBreakTime saveBreakTime(ShopBreakTime breakTime) {
        if (breakTime.getId() == null) {
            ShopBreakTimeJpaEntity saved = shopBreakTimeJpaRepository.save(ShopBreakTimeMapper.toEntity(breakTime));
            return ShopBreakTimeMapper.toDomain(saved);
        }

        ShopBreakTimeJpaEntity entity = shopBreakTimeJpaRepository.findById(breakTime.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 브레이크타임입니다: " + breakTime.getId()));
        ShopBreakTimeMapper.applyChanges(entity, breakTime);
        return ShopBreakTimeMapper.toDomain(entity);
    }

    @Override
    public void deleteBreakTimeById(Long id) {
        shopBreakTimeJpaRepository.deleteById(id);
    }

    @Override
    public List<ShopClosedDay> findClosedDaysByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopClosedDayJpaEntity)
            .where(shopClosedDayJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopClosedDayMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopClosedDay> findClosedDayById(Long id) {
        return shopClosedDayJpaRepository.findById(id)
            .map(ShopClosedDayMapper::toDomain);
    }

    @Override
    public ShopClosedDay saveClosedDay(ShopClosedDay closedDay) {
        if (closedDay.getId() == null) {
            ShopClosedDayJpaEntity saved = shopClosedDayJpaRepository.save(ShopClosedDayMapper.toEntity(closedDay));
            return ShopClosedDayMapper.toDomain(saved);
        }

        ShopClosedDayJpaEntity entity = shopClosedDayJpaRepository.findById(closedDay.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 정기 휴무입니다: " + closedDay.getId()));
        return ShopClosedDayMapper.toDomain(entity);
    }

    @Override
    public void deleteClosedDayById(Long id) {
        shopClosedDayJpaRepository.deleteById(id);
    }
}
