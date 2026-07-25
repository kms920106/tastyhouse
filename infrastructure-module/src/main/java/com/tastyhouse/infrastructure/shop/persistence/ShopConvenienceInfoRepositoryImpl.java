package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopConvenienceInfo;
import com.tastyhouse.core.domain.shop.domain.repository.ShopConvenienceInfoRepository;

@Repository
@RequiredArgsConstructor
public class ShopConvenienceInfoRepositoryImpl implements ShopConvenienceInfoRepository {

    private final ShopConvenienceInfoJpaRepository shopConvenienceInfoJpaRepository;

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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopConvenienceInfoJpaEntity entity = shopConvenienceInfoJpaRepository.findById(shopConvenienceInfo.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 편의정보입니다: " + shopConvenienceInfo.getId()));
        ShopConvenienceInfoMapper.applyChanges(entity, shopConvenienceInfo);
        return ShopConvenienceInfoMapper.toDomain(entity);
    }
}
