package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopPhoneNumberJpaEntity.shopPhoneNumberJpaEntity;

@Repository
class ShopPhoneNumberPersistenceAdapter implements ShopPhoneNumberLoadPort, ShopPhoneNumberSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopPhoneNumberJpaRepository shopPhoneNumberJpaRepository;

    public ShopPhoneNumberPersistenceAdapter(JPAQueryFactory queryFactory, ShopPhoneNumberJpaRepository shopPhoneNumberJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopPhoneNumberJpaRepository = shopPhoneNumberJpaRepository;
    }

    @Override
    public ShopPhoneNumber save(ShopPhoneNumber shopPhoneNumber) {
        if (shopPhoneNumber.getId() == null) {
            ShopPhoneNumberJpaEntity saved = shopPhoneNumberJpaRepository.save(ShopPhoneNumberMapper.toEntity(shopPhoneNumber));
            return ShopPhoneNumberMapper.toDomain(saved);
        }

        ShopPhoneNumberJpaEntity entity = shopPhoneNumberJpaRepository.findById(shopPhoneNumber.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 전화번호입니다: " + shopPhoneNumber.getId()));
        ShopPhoneNumberMapper.applyChanges(entity, shopPhoneNumber);
        return ShopPhoneNumberMapper.toDomain(entity);
    }

    @Override
    public List<ShopPhoneNumber> findByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopPhoneNumberJpaEntity)
            .where(shopPhoneNumberJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ShopPhoneNumberMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopPhoneNumber> findById(Long id) {
        return shopPhoneNumberJpaRepository.findById(id).map(ShopPhoneNumberMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopPhoneNumberJpaRepository.deleteById(id);
    }
}
