package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureState;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureStatePort;

@Repository
public class ShopTemporaryClosureStatePortImpl implements ShopTemporaryClosureStatePort {
    private final ShopTemporaryClosureJpaRepository shopTemporaryClosureJpaRepository;

    public ShopTemporaryClosureStatePortImpl(ShopTemporaryClosureJpaRepository shopTemporaryClosureJpaRepository) {
        this.shopTemporaryClosureJpaRepository = shopTemporaryClosureJpaRepository;
    }

    @Override
    public ShopTemporaryClosureState save(ShopTemporaryClosureState shopTemporaryClosure) {
        ShopTemporaryClosureJpaEntity saved = shopTemporaryClosureJpaRepository.save(ShopTemporaryClosureMapper.toEntity(shopTemporaryClosure));
        return ShopTemporaryClosureMapper.toState(saved);
    }

    @Override
    public List<ShopTemporaryClosureState> findByShopId(Long shopId) {
        return shopTemporaryClosureJpaRepository.findByShopId(shopId)
            .stream()
            .map(ShopTemporaryClosureMapper::toState)
            .toList();
    }

    @Override
    public Optional<ShopTemporaryClosureState> findById(Long id) {
        return shopTemporaryClosureJpaRepository.findById(id).map(ShopTemporaryClosureMapper::toState);
    }

    @Override
    public void deleteById(Long id) {
        shopTemporaryClosureJpaRepository.deleteById(id);
    }
}
