package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeState;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeStatePort;

@Repository
public class ShopOrderNoticeStatePortImpl implements ShopOrderNoticeStatePort {
    private final ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository;

    public ShopOrderNoticeStatePortImpl(ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository) {
        this.shopOrderNoticeJpaRepository = shopOrderNoticeJpaRepository;
    }

    @Override
    public ShopOrderNoticeState save(ShopOrderNoticeState shopOrderNotice) {
        if (shopOrderNotice.id() == null) {
            ShopOrderNoticeJpaEntity saved =
                shopOrderNoticeJpaRepository.save(ShopOrderNoticeMapper.toEntity(shopOrderNotice));
            return ShopOrderNoticeMapper.toState(saved);
        }

        ShopOrderNoticeJpaEntity entity = shopOrderNoticeJpaRepository.findById(shopOrderNotice.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문안내입니다: " + shopOrderNotice.id()));
        ShopOrderNoticeMapper.applyChanges(entity, shopOrderNotice);
        return ShopOrderNoticeMapper.toState(entity);
    }

    @Override
    public Optional<ShopOrderNoticeState> findByShopId(Long shopId) {
        return shopOrderNoticeJpaRepository.findByShopId(shopId)
            .map(ShopOrderNoticeMapper::toState);
    }
}
