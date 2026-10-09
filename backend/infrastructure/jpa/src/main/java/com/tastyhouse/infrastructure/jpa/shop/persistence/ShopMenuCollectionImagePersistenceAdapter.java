package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopMenuCollectionImageJpaEntity.shopMenuCollectionImageJpaEntity;

@Repository
class ShopMenuCollectionImagePersistenceAdapter implements ShopMenuCollectionImageLoadPort, ShopMenuCollectionImageSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository;

    public ShopMenuCollectionImagePersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopMenuCollectionImageJpaRepository = shopMenuCollectionImageJpaRepository;
    }

    @Override
    public ShopMenuCollectionImage save(ShopMenuCollectionImage image) {
        if (image.getId() == null) {
            ShopMenuCollectionImageJpaEntity saved =
                shopMenuCollectionImageJpaRepository.save(ShopMenuCollectionImageMapper.toEntity(image));
            return ShopMenuCollectionImageMapper.toDomain(saved);
        }

        ShopMenuCollectionImageJpaEntity entity = shopMenuCollectionImageJpaRepository.findById(image.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴모음컷입니다: " + image.getId()));
        ShopMenuCollectionImageMapper.applyChanges(entity, image);
        return ShopMenuCollectionImageMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopMenuCollectionImage> findById(ShopMenuCollectionImageId id) {
        return shopMenuCollectionImageJpaRepository.findById(id.value())
            .map(ShopMenuCollectionImageMapper::toDomain);
    }

    @Override
    public List<ShopMenuCollectionImage> findAllByShopId(ShopId shopId) {
        return queryFactory
            .selectFrom(shopMenuCollectionImageJpaEntity)
            .where(shopMenuCollectionImageJpaEntity.shopId.eq(shopId.value()))
            .orderBy(shopMenuCollectionImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ShopMenuCollectionImageMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ShopMenuCollectionImage image) {
        shopMenuCollectionImageJpaRepository.deleteById(image.getId());
    }
}
