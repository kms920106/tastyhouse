package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingState;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingStatePort;

@Repository
public class ShopReviewDisplaySettingStatePortImpl implements ShopReviewDisplaySettingStatePort {
    private final ShopReviewDisplaySettingJpaRepository shopReviewDisplaySettingJpaRepository;

    public ShopReviewDisplaySettingStatePortImpl(ShopReviewDisplaySettingJpaRepository shopReviewDisplaySettingJpaRepository) {
        this.shopReviewDisplaySettingJpaRepository = shopReviewDisplaySettingJpaRepository;
    }

    @Override
    public Optional<ShopReviewDisplaySettingState> findByShopId(Long shopId) {
        return shopReviewDisplaySettingJpaRepository.findByShopId(shopId)
            .map(ShopReviewDisplaySettingMapper::toState);
    }

    @Override
    public ShopReviewDisplaySettingState save(ShopReviewDisplaySettingState state) {
        if (state.id() == null) {
            ShopReviewDisplaySettingJpaEntity saved =
                shopReviewDisplaySettingJpaRepository.save(ShopReviewDisplaySettingMapper.toEntity(state));
            return ShopReviewDisplaySettingMapper.toState(saved);
        }

        ShopReviewDisplaySettingJpaEntity entity = shopReviewDisplaySettingJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 노출 정렬 설정입니다: " + state.id()));
        ShopReviewDisplaySettingMapper.applyChanges(entity, state);
        return ShopReviewDisplaySettingMapper.toState(entity);
    }
}
