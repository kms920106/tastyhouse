package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.domain.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.domain.repository.ShopTemporaryClosureRepository;

@Repository
@RequiredArgsConstructor
public class ShopTemporaryClosureRepositoryImpl implements ShopTemporaryClosureRepository {

    private final ShopTemporaryClosureJpaRepository shopTemporaryClosureJpaRepository;

    @Override
    public ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure) {
        ShopTemporaryClosureJpaEntity saved = shopTemporaryClosureJpaRepository.save(ShopTemporaryClosureMapper.toEntity(shopTemporaryClosure));
        return ShopTemporaryClosureMapper.toDomain(saved);
    }

    @Override
    public List<ShopTemporaryClosure> findByShopId(Long shopId) {
        return shopTemporaryClosureJpaRepository.findByShopId(shopId)
            .stream()
            .map(ShopTemporaryClosureMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopTemporaryClosure> findById(Long id) {
        return shopTemporaryClosureJpaRepository.findById(id).map(ShopTemporaryClosureMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopTemporaryClosureJpaRepository.deleteById(id);
    }
}
