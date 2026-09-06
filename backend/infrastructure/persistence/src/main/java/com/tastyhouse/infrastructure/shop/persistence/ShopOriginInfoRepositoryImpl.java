package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.repository.ShopOriginInfoRepository;

@Repository
public class ShopOriginInfoRepositoryImpl implements ShopOriginInfoRepository {
    private final ShopOriginInfoJpaRepository shopOriginInfoJpaRepository;

    public ShopOriginInfoRepositoryImpl(ShopOriginInfoJpaRepository shopOriginInfoJpaRepository) {
        this.shopOriginInfoJpaRepository = shopOriginInfoJpaRepository;
    }

    @Override
    public Optional<ShopOriginInfo> findByShopId(Long shopId) {
        return shopOriginInfoJpaRepository.findByShopId(shopId).map(ShopOriginInfoMapper::toDomain);
    }

    @Override
    public ShopOriginInfo save(ShopOriginInfo shopOriginInfo) {
        if (shopOriginInfo.getId() == null) {
            ShopOriginInfoJpaEntity saved = shopOriginInfoJpaRepository.save(ShopOriginInfoMapper.toEntity(shopOriginInfo));
            return ShopOriginInfoMapper.toDomain(saved);
        }

        ShopOriginInfoJpaEntity entity = shopOriginInfoJpaRepository.findById(shopOriginInfo.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 원산지 정보입니다: " + shopOriginInfo.getId()));
        ShopOriginInfoMapper.applyChanges(entity, shopOriginInfo);
        return ShopOriginInfoMapper.toDomain(entity);
    }
}
