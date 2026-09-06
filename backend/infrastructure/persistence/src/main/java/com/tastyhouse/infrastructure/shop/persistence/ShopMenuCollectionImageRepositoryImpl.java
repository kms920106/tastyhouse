package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.repository.ShopMenuCollectionImageRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

@Repository
public class ShopMenuCollectionImageRepositoryImpl implements ShopMenuCollectionImageRepository {
    private final ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository;

    public ShopMenuCollectionImageRepositoryImpl(
        ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository
    ) {
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
        return shopMenuCollectionImageJpaRepository.findAllByShopIdOrderBySortAsc(shopId.value()).stream()
            .map(ShopMenuCollectionImageMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ShopMenuCollectionImage image) {
        shopMenuCollectionImageJpaRepository.deleteById(image.getId());
    }
}
