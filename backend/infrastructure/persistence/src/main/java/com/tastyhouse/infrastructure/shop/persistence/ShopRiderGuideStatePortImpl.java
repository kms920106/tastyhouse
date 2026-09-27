package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideHistoryState;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideState;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideStatePort;

@Repository
public class ShopRiderGuideStatePortImpl implements ShopRiderGuideStatePort {
    private final ShopRiderGuideJpaRepository shopRiderGuideJpaRepository;
    private final ShopRiderGuideHistoryJpaRepository shopRiderGuideHistoryJpaRepository;

    public ShopRiderGuideStatePortImpl(
        ShopRiderGuideJpaRepository shopRiderGuideJpaRepository,
        ShopRiderGuideHistoryJpaRepository shopRiderGuideHistoryJpaRepository
    ) {
        this.shopRiderGuideJpaRepository = shopRiderGuideJpaRepository;
        this.shopRiderGuideHistoryJpaRepository = shopRiderGuideHistoryJpaRepository;
    }

    @Override
    public Optional<ShopRiderGuideState> findByShopId(Long shopId) {
        return shopRiderGuideJpaRepository.findByShopId(shopId)
            .map(ShopRiderGuideMapper::toState);
    }

    @Override
    public ShopRiderGuideState save(ShopRiderGuideState riderGuide) {
        if (riderGuide.id() == null) {
            ShopRiderGuideJpaEntity saved = shopRiderGuideJpaRepository.save(ShopRiderGuideMapper.toEntity(riderGuide));
            return ShopRiderGuideMapper.toState(saved);
        }

        ShopRiderGuideJpaEntity entity = shopRiderGuideJpaRepository.findById(riderGuide.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 라이더 안내입니다: " + riderGuide.id()));
        ShopRiderGuideMapper.applyChanges(entity, riderGuide);
        return ShopRiderGuideMapper.toState(entity);
    }

    @Override
    public ShopRiderGuideHistoryState saveHistory(ShopRiderGuideHistoryState history) {
        ShopRiderGuideHistoryJpaEntity saved = shopRiderGuideHistoryJpaRepository
            .save(ShopRiderGuideHistoryMapper.toEntity(history));
        return ShopRiderGuideHistoryMapper.toState(saved);
    }
}
