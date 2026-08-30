package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.review.repository.ShopReviewDisplaySettingRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ShopReviewDisplaySettingRepositoryImpl implements ShopReviewDisplaySettingRepository {

    private final ShopReviewDisplaySettingJpaRepository shopReviewDisplaySettingJpaRepository;

    public ShopReviewDisplaySettingRepositoryImpl(ShopReviewDisplaySettingJpaRepository shopReviewDisplaySettingJpaRepository) {
        this.shopReviewDisplaySettingJpaRepository = shopReviewDisplaySettingJpaRepository;
    }

    @Override
    public Optional<ShopReviewDisplaySetting> findByShopId(ShopId shopId) {
        return shopReviewDisplaySettingJpaRepository.findByShopId(shopId.value())
            .map(ShopReviewDisplaySettingMapper::toDomain);
    }

    @Override
    public ShopReviewDisplaySetting save(ShopReviewDisplaySetting shopReviewDisplaySetting) {
        if (shopReviewDisplaySetting.getId() == null) {
            ShopReviewDisplaySettingJpaEntity saved =
                shopReviewDisplaySettingJpaRepository.save(ShopReviewDisplaySettingMapper.toEntity(shopReviewDisplaySetting));
            return ShopReviewDisplaySettingMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopReviewDisplaySettingJpaEntity entity = shopReviewDisplaySettingJpaRepository.findById(shopReviewDisplaySetting.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 노출 정렬 설정입니다: " + shopReviewDisplaySetting.getId()));
        ShopReviewDisplaySettingMapper.applyChanges(entity, shopReviewDisplaySetting);
        return ShopReviewDisplaySettingMapper.toDomain(entity);
    }
}
