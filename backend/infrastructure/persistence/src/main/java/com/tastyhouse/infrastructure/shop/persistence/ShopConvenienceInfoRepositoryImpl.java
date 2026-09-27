package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoRepository;
import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;

@Repository
public class ShopConvenienceInfoRepositoryImpl implements ShopConvenienceInfoRepository {
    private final ShopConvenienceInfoJpaRepository shopConvenienceInfoJpaRepository;

    public ShopConvenienceInfoRepositoryImpl(ShopConvenienceInfoJpaRepository shopConvenienceInfoJpaRepository) {
        this.shopConvenienceInfoJpaRepository = shopConvenienceInfoJpaRepository;
    }

    @Override
    public Optional<ShopConvenienceInfo> findByShopId(Long shopId) {
        return shopConvenienceInfoJpaRepository.findByShopId(shopId).map(ShopConvenienceInfoMapper::toDomain);
    }

    @Override
    public ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo) {
        if (shopConvenienceInfo.getId() == null) {
            ShopConvenienceInfoJpaEntity saved = shopConvenienceInfoJpaRepository.save(ShopConvenienceInfoMapper.toEntity(shopConvenienceInfo));
            return ShopConvenienceInfoMapper.toDomain(saved);
        }

        ShopConvenienceInfoJpaEntity entity = shopConvenienceInfoJpaRepository.findById(shopConvenienceInfo.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 편의정보입니다: " + shopConvenienceInfo.getId()));
        ShopConvenienceInfoMapper.applyChanges(entity, shopConvenienceInfo);
        return ShopConvenienceInfoMapper.toDomain(entity);
    }
}
