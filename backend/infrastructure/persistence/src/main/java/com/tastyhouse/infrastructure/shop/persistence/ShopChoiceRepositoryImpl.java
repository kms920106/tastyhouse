package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.repository.ShopChoiceRepository;

@Repository
public class ShopChoiceRepositoryImpl implements ShopChoiceRepository {
    private final ShopChoiceJpaRepository shopChoiceJpaRepository;

    public ShopChoiceRepositoryImpl(ShopChoiceJpaRepository shopChoiceJpaRepository) {
        this.shopChoiceJpaRepository = shopChoiceJpaRepository;
    }

    @Override
    public Optional<ShopChoice> findById(Long id) {
        return shopChoiceJpaRepository.findById(id).map(ShopChoiceMapper::toDomain);
    }

    @Override
    public ShopChoice save(ShopChoice shopChoice) {
        if (shopChoice.getId() == null) {
            ShopChoiceJpaEntity saved = shopChoiceJpaRepository.save(ShopChoiceMapper.toEntity(shopChoice));
            return ShopChoiceMapper.toDomain(saved);
        }

        ShopChoiceJpaEntity entity = shopChoiceJpaRepository.findById(shopChoice.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 에디터 초이스입니다: " + shopChoice.getId()));
        ShopChoiceMapper.applyChanges(entity, shopChoice);
        return ShopChoiceMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        shopChoiceJpaRepository.deleteById(id);
    }
}
