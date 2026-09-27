package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageState;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageStatePort;

@Repository
public class ShopMenuCollectionImageStatePortImpl implements ShopMenuCollectionImageStatePort {
    private final ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository;

    public ShopMenuCollectionImageStatePortImpl(
        ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository
    ) {
        this.shopMenuCollectionImageJpaRepository = shopMenuCollectionImageJpaRepository;
    }

    @Override
    public ShopMenuCollectionImageState save(ShopMenuCollectionImageState image) {
        if (image.id() == null) {
            ShopMenuCollectionImageJpaEntity saved =
                shopMenuCollectionImageJpaRepository.save(ShopMenuCollectionImageMapper.toEntity(image));
            return ShopMenuCollectionImageMapper.toState(saved);
        }

        ShopMenuCollectionImageJpaEntity entity = shopMenuCollectionImageJpaRepository.findById(image.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴모음컷입니다: " + image.id()));
        ShopMenuCollectionImageMapper.applyChanges(entity, image);
        return ShopMenuCollectionImageMapper.toState(entity);
    }

    @Override
    public Optional<ShopMenuCollectionImageState> findById(Long id) {
        return shopMenuCollectionImageJpaRepository.findById(id)
            .map(ShopMenuCollectionImageMapper::toState);
    }

    @Override
    public List<ShopMenuCollectionImageState> findAllByShopId(Long shopId) {
        return shopMenuCollectionImageJpaRepository.findAllByShopIdOrderBySortAsc(shopId).stream()
            .map(ShopMenuCollectionImageMapper::toState)
            .toList();
    }

    @Override
    public void delete(ShopMenuCollectionImageState image) {
        shopMenuCollectionImageJpaRepository.deleteById(image.id());
    }
}
