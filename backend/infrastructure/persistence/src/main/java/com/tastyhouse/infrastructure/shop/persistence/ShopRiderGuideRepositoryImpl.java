package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.repository.ShopRiderGuideRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

@Repository
public class ShopRiderGuideRepositoryImpl implements ShopRiderGuideRepository {
    private final ShopRiderGuideJpaRepository shopRiderGuideJpaRepository;
    private final ShopRiderGuideHistoryJpaRepository shopRiderGuideHistoryJpaRepository;

    public ShopRiderGuideRepositoryImpl(
        ShopRiderGuideJpaRepository shopRiderGuideJpaRepository,
        ShopRiderGuideHistoryJpaRepository shopRiderGuideHistoryJpaRepository
    ) {
        this.shopRiderGuideJpaRepository = shopRiderGuideJpaRepository;
        this.shopRiderGuideHistoryJpaRepository = shopRiderGuideHistoryJpaRepository;
    }

    @Override
    public Optional<ShopRiderGuide> findByShopId(ShopId shopId) {
        return shopRiderGuideJpaRepository.findByShopId(IdMapping.raw(shopId, ShopId::value))
            .map(ShopRiderGuideMapper::toDomain);
    }

    @Override
    public ShopRiderGuide save(ShopRiderGuide riderGuide) {
        if (riderGuide.getId() == null) {
            ShopRiderGuideJpaEntity saved = shopRiderGuideJpaRepository.save(ShopRiderGuideMapper.toEntity(riderGuide));
            return ShopRiderGuideMapper.toDomain(saved);
        }

        ShopRiderGuideJpaEntity entity = shopRiderGuideJpaRepository.findById(riderGuide.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 라이더 안내입니다: " + riderGuide.getId()));
        ShopRiderGuideMapper.applyChanges(entity, riderGuide);
        return ShopRiderGuideMapper.toDomain(entity);
    }

    @Override
    public ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history) {
        ShopRiderGuideHistoryJpaEntity saved = shopRiderGuideHistoryJpaRepository
            .save(ShopRiderGuideHistoryMapper.toEntity(history));
        return ShopRiderGuideHistoryMapper.toDomain(saved);
    }
}
