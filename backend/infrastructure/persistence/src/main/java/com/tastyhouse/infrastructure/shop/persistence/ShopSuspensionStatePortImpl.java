package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopSuspensionState;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionStatePort;

@Repository
public class ShopSuspensionStatePortImpl implements ShopSuspensionStatePort {
    private final ShopSuspensionJpaRepository shopSuspensionJpaRepository;

    public ShopSuspensionStatePortImpl(ShopSuspensionJpaRepository shopSuspensionJpaRepository) {
        this.shopSuspensionJpaRepository = shopSuspensionJpaRepository;
    }

    @Override
    public ShopSuspensionState save(ShopSuspensionState shopSuspension) {
        if (shopSuspension.id() == null) {
            ShopSuspensionJpaEntity saved = shopSuspensionJpaRepository.save(ShopSuspensionMapper.toEntity(shopSuspension));
            return ShopSuspensionMapper.toState(saved);
        }

        ShopSuspensionJpaEntity entity = shopSuspensionJpaRepository.findById(shopSuspension.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 영업 임시중지입니다: " + shopSuspension.id()));
        ShopSuspensionMapper.applyChanges(entity, shopSuspension);
        return ShopSuspensionMapper.toState(entity);
    }

    @Override
    public List<ShopSuspensionState> findByShopId(Long shopId) {
        return shopSuspensionJpaRepository.findByShopId(shopId)
            .stream()
            .map(ShopSuspensionMapper::toState)
            .toList();
    }

    @Override
    public Optional<ShopSuspensionState> findById(Long id) {
        return shopSuspensionJpaRepository.findById(id).map(ShopSuspensionMapper::toState);
    }
}
