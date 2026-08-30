package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.repository.ShopOrderNoticeRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ShopOrderNoticeRepositoryImpl implements ShopOrderNoticeRepository {

    private final ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository;

    public ShopOrderNoticeRepositoryImpl(ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository) {
        this.shopOrderNoticeJpaRepository = shopOrderNoticeJpaRepository;
    }

    @Override
    public ShopOrderNotice save(ShopOrderNotice shopOrderNotice) {
        if (shopOrderNotice.getId() == null) {
            ShopOrderNoticeJpaEntity saved =
                shopOrderNoticeJpaRepository.save(ShopOrderNoticeMapper.toEntity(shopOrderNotice));
            return ShopOrderNoticeMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopOrderNoticeJpaEntity entity = shopOrderNoticeJpaRepository.findById(shopOrderNotice.getId().value())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문안내입니다: " + shopOrderNotice.getId().value()));
        ShopOrderNoticeMapper.applyChanges(entity, shopOrderNotice);
        return ShopOrderNoticeMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopOrderNotice> findByShopId(ShopId shopId) {
        return shopOrderNoticeJpaRepository.findByShopId(shopId.value())
            .map(ShopOrderNoticeMapper::toDomain);
    }
}
