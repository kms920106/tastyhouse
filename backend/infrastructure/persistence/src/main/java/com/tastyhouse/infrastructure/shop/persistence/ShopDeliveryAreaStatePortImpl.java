package com.tastyhouse.infrastructure.shop.persistence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaState;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaStatePort;

@Repository
public class ShopDeliveryAreaStatePortImpl implements ShopDeliveryAreaStatePort {
    private final ShopDeliveryAreaJpaRepository shopDeliveryAreaJpaRepository;

    public ShopDeliveryAreaStatePortImpl(ShopDeliveryAreaJpaRepository shopDeliveryAreaJpaRepository) {
        this.shopDeliveryAreaJpaRepository = shopDeliveryAreaJpaRepository;
    }

    @Override
    public List<ShopDeliveryAreaState> findByShopId(Long shopId) {
        return shopDeliveryAreaJpaRepository.findByShopIdOrderByIdAsc(shopId).stream()
            .map(ShopDeliveryAreaMapper::toState)
            .toList();
    }

    @Override
    public Optional<ShopDeliveryAreaState> findById(Long deliveryAreaId) {
        return shopDeliveryAreaJpaRepository.findById(deliveryAreaId)
            .map(ShopDeliveryAreaMapper::toState);
    }

    @Override
    public boolean existsByShopIdAndAdminDongId(Long shopId, Long adminDongId) {
        return shopDeliveryAreaJpaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId);
    }

    @Override
    public long countByShopId(Long shopId) {
        return shopDeliveryAreaJpaRepository.countByShopId(shopId);
    }

    @Override
    public ShopDeliveryAreaState save(ShopDeliveryAreaState shopDeliveryArea) {
        ShopDeliveryAreaJpaEntity saved = shopDeliveryAreaJpaRepository.save(ShopDeliveryAreaMapper.toEntity(shopDeliveryArea));
        return ShopDeliveryAreaMapper.toState(saved);
    }

    @Override
    public List<ShopDeliveryAreaState> saveAll(List<ShopDeliveryAreaState> shopDeliveryAreas) {
        List<ShopDeliveryAreaJpaEntity> entities = shopDeliveryAreas.stream()
            .map(ShopDeliveryAreaMapper::toEntity)
            .toList();

        return shopDeliveryAreaJpaRepository.saveAll(entities).stream()
            .map(ShopDeliveryAreaMapper::toState)
            .toList();
    }

    @Override
    public List<ShopDeliveryAreaState> findByShopIdAndSource(Long shopId, String source) {
        return shopDeliveryAreaJpaRepository.findByShopIdAndSource(shopId, source).stream()
            .map(ShopDeliveryAreaMapper::toState)
            .toList();
    }

    @Override
    public void deleteByShopIdAndSource(Long shopId, String source) {
        shopDeliveryAreaJpaRepository.deleteByShopIdAndSource(shopId, source);
    }

    @Override
    public Set<Long> findAdminDongIdsByShopId(Long shopId) {
        return new LinkedHashSet<>(shopDeliveryAreaJpaRepository.findAdminDongIdsByShopId(shopId));
    }

    @Override
    public void deleteById(Long deliveryAreaId) {
        shopDeliveryAreaJpaRepository.deleteById(deliveryAreaId);
    }
}
