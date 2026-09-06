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
