package com.tastyhouse.application.shop.store;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaStatePort;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryAreaStore implements ShopDeliveryAreaRepository {
    private final ShopDeliveryAreaStatePort shopDeliveryAreaStatePort;

    public ShopDeliveryAreaStore(ShopDeliveryAreaStatePort shopDeliveryAreaStatePort) {
        this.shopDeliveryAreaStatePort = shopDeliveryAreaStatePort;
    }

    @Override
    public List<ShopDeliveryArea> findByShopId(ShopId shopId) {
        return shopDeliveryAreaStatePort.findByShopId(shopId.value()).stream()
            .map(ShopDeliveryAreaStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopDeliveryArea> findById(Long deliveryAreaId) {
        return shopDeliveryAreaStatePort.findById(deliveryAreaId).map(ShopDeliveryAreaStateMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
        return shopDeliveryAreaStatePort.existsByShopIdAndAdminDongId(shopId.value(), adminDongId.value());
    }

    @Override
    public long countByShopId(ShopId shopId) {
        return shopDeliveryAreaStatePort.countByShopId(shopId.value());
    }

    @Override
    public ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea) {
        return ShopDeliveryAreaStateMapper.toDomain(shopDeliveryAreaStatePort.save(ShopDeliveryAreaStateMapper.toState(shopDeliveryArea)));
    }

    @Override
    public List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas) {
        return shopDeliveryAreaStatePort.saveAll(shopDeliveryAreas.stream().map(ShopDeliveryAreaStateMapper::toState).toList()).stream()
            .map(ShopDeliveryAreaStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ShopDeliveryArea> findByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
        return shopDeliveryAreaStatePort.findByShopIdAndSource(shopId.value(), source == null ? null : source.name())
            .stream()
            .map(ShopDeliveryAreaStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
        shopDeliveryAreaStatePort.deleteByShopIdAndSource(shopId.value(), source == null ? null : source.name());
    }

    @Override
    public Set<AdminDongId> findAdminDongIdsByShopId(ShopId shopId) {
        return shopDeliveryAreaStatePort.findAdminDongIdsByShopId(shopId.value()).stream()
            .map(AdminDongId::of)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void deleteById(Long deliveryAreaId) {
        shopDeliveryAreaStatePort.deleteById(deliveryAreaId);
    }
}
