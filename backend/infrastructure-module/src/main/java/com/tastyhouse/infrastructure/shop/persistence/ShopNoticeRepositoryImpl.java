package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ShopNoticeRepositoryImpl implements ShopNoticeRepository {

    private final ShopNoticeJpaRepository shopNoticeJpaRepository;

    public ShopNoticeRepositoryImpl(ShopNoticeJpaRepository shopNoticeJpaRepository) {
        this.shopNoticeJpaRepository = shopNoticeJpaRepository;
    }

    @Override
    public ShopNotice save(ShopNotice shopNotice) {
        if (shopNotice.getId() == null) {
            ShopNoticeJpaEntity saved = shopNoticeJpaRepository.save(ShopNoticeMapper.toEntity(shopNotice));
            return ShopNoticeMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopNoticeJpaEntity entity = shopNoticeJpaRepository.findById(shopNotice.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 점주 공지입니다: " + shopNotice.getId()));
        ShopNoticeMapper.applyChanges(entity, shopNotice);
        return ShopNoticeMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopNotice> findById(Long id) {
        return shopNoticeJpaRepository.findById(id).map(ShopNoticeMapper::toDomain);
    }

    @Override
    public Optional<ShopNotice> findExposedByShopId(ShopId shopId) {
        return shopNoticeJpaRepository.findFirstByShopIdAndExposedIsTrueOrderByIdDesc(shopId.value())
            .map(ShopNoticeMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopNoticeJpaRepository.deleteById(id);
    }
}
