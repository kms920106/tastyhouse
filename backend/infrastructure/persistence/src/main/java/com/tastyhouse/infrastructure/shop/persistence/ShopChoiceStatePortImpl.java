package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopChoiceState;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceStatePort;

@Repository
public class ShopChoiceStatePortImpl implements ShopChoiceStatePort {
    private final ShopChoiceJpaRepository shopChoiceJpaRepository;

    public ShopChoiceStatePortImpl(ShopChoiceJpaRepository shopChoiceJpaRepository) {
        this.shopChoiceJpaRepository = shopChoiceJpaRepository;
    }

    @Override
    public Optional<ShopChoiceState> findById(Long id) {
        return shopChoiceJpaRepository.findById(id).map(ShopChoiceMapper::toState);
    }

    @Override
    public ShopChoiceState save(ShopChoiceState shopChoice) {
        if (shopChoice.id() == null) {
            ShopChoiceJpaEntity saved = shopChoiceJpaRepository.save(ShopChoiceMapper.toEntity(shopChoice));
            return ShopChoiceMapper.toState(saved);
        }

        ShopChoiceJpaEntity entity = shopChoiceJpaRepository.findById(shopChoice.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 에디터 초이스입니다: " + shopChoice.id()));
        ShopChoiceMapper.applyChanges(entity, shopChoice);
        return ShopChoiceMapper.toState(entity);
    }

    @Override
    public void deleteById(Long id) {
        shopChoiceJpaRepository.deleteById(id);
    }
}
