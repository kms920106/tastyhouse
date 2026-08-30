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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopOriginInfoJpaEntity entity = shopOriginInfoJpaRepository.findById(shopOriginInfo.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 원산지 정보입니다: " + shopOriginInfo.getId()));
        ShopOriginInfoMapper.applyChanges(entity, shopOriginInfo);
        return ShopOriginInfoMapper.toDomain(entity);
    }
}
