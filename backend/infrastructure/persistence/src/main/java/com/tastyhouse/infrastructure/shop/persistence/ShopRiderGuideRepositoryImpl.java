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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
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
