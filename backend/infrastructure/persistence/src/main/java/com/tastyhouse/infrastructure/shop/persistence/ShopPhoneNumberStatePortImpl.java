package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberState;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberStatePort;

@Repository
public class ShopPhoneNumberStatePortImpl implements ShopPhoneNumberStatePort {
    private final ShopPhoneNumberJpaRepository shopPhoneNumberJpaRepository;

    public ShopPhoneNumberStatePortImpl(ShopPhoneNumberJpaRepository shopPhoneNumberJpaRepository) {
        this.shopPhoneNumberJpaRepository = shopPhoneNumberJpaRepository;
    }

    @Override
    public ShopPhoneNumberState save(ShopPhoneNumberState shopPhoneNumber) {
        if (shopPhoneNumber.id() == null) {
            ShopPhoneNumberJpaEntity saved = shopPhoneNumberJpaRepository.save(ShopPhoneNumberMapper.toEntity(shopPhoneNumber));
            return ShopPhoneNumberMapper.toState(saved);
        }

        ShopPhoneNumberJpaEntity entity = shopPhoneNumberJpaRepository.findById(shopPhoneNumber.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 전화번호입니다: " + shopPhoneNumber.id()));
        ShopPhoneNumberMapper.applyChanges(entity, shopPhoneNumber);
        return ShopPhoneNumberMapper.toState(entity);
    }

    @Override
    public List<ShopPhoneNumberState> findByShopId(Long shopId) {
        return shopPhoneNumberJpaRepository.findByShopId(shopId).stream()
            .map(ShopPhoneNumberMapper::toState)
            .toList();
    }

    @Override
    public Optional<ShopPhoneNumberState> findById(Long id) {
        return shopPhoneNumberJpaRepository.findById(id).map(ShopPhoneNumberMapper::toState);
    }

    @Override
    public void deleteById(Long id) {
        shopPhoneNumberJpaRepository.deleteById(id);
    }
}
