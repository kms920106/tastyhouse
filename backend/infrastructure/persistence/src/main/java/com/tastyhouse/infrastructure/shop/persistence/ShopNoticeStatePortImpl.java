package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopNoticeState;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeStatePort;

@Repository
public class ShopNoticeStatePortImpl implements ShopNoticeStatePort {
    private final ShopNoticeJpaRepository shopNoticeJpaRepository;

    public ShopNoticeStatePortImpl(ShopNoticeJpaRepository shopNoticeJpaRepository) {
        this.shopNoticeJpaRepository = shopNoticeJpaRepository;
    }

    @Override
    public ShopNoticeState save(ShopNoticeState shopNotice) {
        if (shopNotice.id() == null) {
            ShopNoticeJpaEntity saved = shopNoticeJpaRepository.save(ShopNoticeMapper.toEntity(shopNotice));
            return ShopNoticeMapper.toState(saved);
        }

        ShopNoticeJpaEntity entity = shopNoticeJpaRepository.findById(shopNotice.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 점주 공지입니다: " + shopNotice.id()));
        ShopNoticeMapper.applyChanges(entity, shopNotice);
        return ShopNoticeMapper.toState(entity);
    }

    @Override
    public Optional<ShopNoticeState> findById(Long id) {
        return shopNoticeJpaRepository.findById(id).map(ShopNoticeMapper::toState);
    }

    @Override
    public Optional<ShopNoticeState> findExposedByShopId(Long shopId) {
        return shopNoticeJpaRepository.findFirstByShopIdAndExposedIsTrueOrderByIdDesc(shopId)
            .map(ShopNoticeMapper::toState);
    }

    @Override
    public void deleteById(Long id) {
        shopNoticeJpaRepository.deleteById(id);
    }
}
