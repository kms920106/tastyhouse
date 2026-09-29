package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticePersistencePort;

@Repository
public class ShopOrderNoticePersistenceAdapter implements ShopOrderNoticePersistencePort {
    private final ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository;

    public ShopOrderNoticePersistenceAdapter(ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository) {
        this.shopOrderNoticeJpaRepository = shopOrderNoticeJpaRepository;
    }

    @Override
    public ShopOrderNotice save(ShopOrderNotice shopOrderNotice) {
        Long id = shopOrderNotice.getId() == null ? null : shopOrderNotice.getId().value();
        if (id == null) {
            ShopOrderNoticeJpaEntity saved =
                shopOrderNoticeJpaRepository.save(ShopOrderNoticeMapper.toEntity(shopOrderNotice));
            return ShopOrderNoticeMapper.toDomain(saved);
        }

        ShopOrderNoticeJpaEntity entity = shopOrderNoticeJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문안내입니다: " + id));
        ShopOrderNoticeMapper.applyChanges(entity, shopOrderNotice);
        return ShopOrderNoticeMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopOrderNotice> findByShopId(ShopId shopId) {
        return shopOrderNoticeJpaRepository.findByShopId(shopId.value())
            .map(ShopOrderNoticeMapper::toDomain);
    }
}
