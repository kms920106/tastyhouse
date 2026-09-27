package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoState;
import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoStatePort;

@Repository
public class ShopConvenienceInfoStatePortImpl implements ShopConvenienceInfoStatePort {
    private final ShopConvenienceInfoJpaRepository shopConvenienceInfoJpaRepository;

    public ShopConvenienceInfoStatePortImpl(ShopConvenienceInfoJpaRepository shopConvenienceInfoJpaRepository) {
        this.shopConvenienceInfoJpaRepository = shopConvenienceInfoJpaRepository;
    }

    @Override
    public Optional<ShopConvenienceInfoState> findByShopId(Long shopId) {
        return shopConvenienceInfoJpaRepository.findByShopId(shopId).map(ShopConvenienceInfoMapper::toState);
    }

    @Override
    public ShopConvenienceInfoState save(ShopConvenienceInfoState shopConvenienceInfo) {
        if (shopConvenienceInfo.id() == null) {
            ShopConvenienceInfoJpaEntity saved = shopConvenienceInfoJpaRepository.save(ShopConvenienceInfoMapper.toEntity(shopConvenienceInfo));
            return ShopConvenienceInfoMapper.toState(saved);
        }

        ShopConvenienceInfoJpaEntity entity = shopConvenienceInfoJpaRepository.findById(shopConvenienceInfo.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 편의정보입니다: " + shopConvenienceInfo.id()));
        ShopConvenienceInfoMapper.applyChanges(entity, shopConvenienceInfo);
        return ShopConvenienceInfoMapper.toState(entity);
    }
}
