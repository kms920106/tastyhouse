package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoState;
import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoStatePort;

@Repository
public class ShopOriginInfoStatePortImpl implements ShopOriginInfoStatePort {
    private final ShopOriginInfoJpaRepository shopOriginInfoJpaRepository;

    public ShopOriginInfoStatePortImpl(ShopOriginInfoJpaRepository shopOriginInfoJpaRepository) {
        this.shopOriginInfoJpaRepository = shopOriginInfoJpaRepository;
    }

    @Override
    public Optional<ShopOriginInfoState> findByShopId(Long shopId) {
        return shopOriginInfoJpaRepository.findByShopId(shopId).map(ShopOriginInfoMapper::toState);
    }

    @Override
    public ShopOriginInfoState save(ShopOriginInfoState shopOriginInfo) {
        if (shopOriginInfo.id() == null) {
            ShopOriginInfoJpaEntity saved = shopOriginInfoJpaRepository.save(ShopOriginInfoMapper.toEntity(shopOriginInfo));
            return ShopOriginInfoMapper.toState(saved);
        }

        ShopOriginInfoJpaEntity entity = shopOriginInfoJpaRepository.findById(shopOriginInfo.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 원산지 정보입니다: " + shopOriginInfo.id()));
        ShopOriginInfoMapper.applyChanges(entity, shopOriginInfo);
        return ShopOriginInfoMapper.toState(entity);
    }
}
