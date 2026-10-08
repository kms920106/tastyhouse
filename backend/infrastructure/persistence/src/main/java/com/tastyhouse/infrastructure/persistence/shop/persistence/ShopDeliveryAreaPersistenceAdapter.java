package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPersistencePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopDeliveryAreaJpaEntity.shopDeliveryAreaJpaEntity;

@Repository
class ShopDeliveryAreaPersistenceAdapter implements ShopDeliveryAreaPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ShopDeliveryAreaJpaRepository shopDeliveryAreaJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ShopDeliveryAreaPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopDeliveryAreaJpaRepository shopDeliveryAreaJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopDeliveryAreaJpaRepository = shopDeliveryAreaJpaRepository;
    }

    @Override
    public List<ShopDeliveryArea> findByShopId(ShopId shopId) {
        return queryFactory
            .selectFrom(shopDeliveryAreaJpaEntity)
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId.value()))
            .orderBy(shopDeliveryAreaJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(ShopDeliveryAreaMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopDeliveryArea> findById(Long deliveryAreaId) {
        return shopDeliveryAreaJpaRepository.findById(deliveryAreaId)
            .map(ShopDeliveryAreaMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
        Integer found = queryFactory
            .selectOne()
            .from(shopDeliveryAreaJpaEntity)
            .where(
                shopDeliveryAreaJpaEntity.shopId.eq(shopId.value()),
                shopDeliveryAreaJpaEntity.adminDongId.eq(adminDongId.value())
            )
            .fetchFirst();
        return found != null;
    }

    @Override
    public long countByShopId(ShopId shopId) {
        Long count = queryFactory
            .select(shopDeliveryAreaJpaEntity.count())
            .from(shopDeliveryAreaJpaEntity)
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea) {
        ShopDeliveryAreaJpaEntity saved = shopDeliveryAreaJpaRepository.save(ShopDeliveryAreaMapper.toEntity(shopDeliveryArea));
        return ShopDeliveryAreaMapper.toDomain(saved);
    }

    @Override
    public List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas) {
        List<ShopDeliveryAreaJpaEntity> entities = shopDeliveryAreas.stream()
            .map(ShopDeliveryAreaMapper::toEntity)
            .toList();

        return shopDeliveryAreaJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryAreaMapper::toDomain)
            .toList();
    }

    @Override
    public List<ShopDeliveryArea> findByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
        return queryFactory
            .selectFrom(shopDeliveryAreaJpaEntity)
            .where(
                shopDeliveryAreaJpaEntity.shopId.eq(shopId.value()),
                sourceEq(source)
            )
            .fetch()
            .stream()
            .map(ShopDeliveryAreaMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
        entityManager.flush();
        if (source == null) {
            entityManager.clear();
            return;
        }
        queryFactory
            .delete(shopDeliveryAreaJpaEntity)
            .where(
                shopDeliveryAreaJpaEntity.shopId.eq(shopId.value()),
                shopDeliveryAreaJpaEntity.source.eq(source.name())
            )
            .execute();
        entityManager.clear();
    }

    @Override
    public Set<AdminDongId> findAdminDongIdsByShopId(ShopId shopId) {
        return queryFactory
            .select(shopDeliveryAreaJpaEntity.adminDongId)
            .from(shopDeliveryAreaJpaEntity)
            .where(shopDeliveryAreaJpaEntity.shopId.eq(shopId.value()))
            .orderBy(shopDeliveryAreaJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(AdminDongId::of)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void deleteById(Long deliveryAreaId) {
        shopDeliveryAreaJpaRepository.deleteById(deliveryAreaId);
    }

    private BooleanExpression sourceEq(DeliveryAreaSource source) {
        return source == null
            ? shopDeliveryAreaJpaEntity.source.isNull()
            : shopDeliveryAreaJpaEntity.source.eq(source.name());
    }
}
