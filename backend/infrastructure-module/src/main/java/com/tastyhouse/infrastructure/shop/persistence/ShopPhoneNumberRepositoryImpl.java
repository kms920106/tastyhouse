package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.repository.ShopPhoneNumberRepository;

@Repository
public class ShopPhoneNumberRepositoryImpl implements ShopPhoneNumberRepository {

    private final ShopPhoneNumberJpaRepository shopPhoneNumberJpaRepository;

    public ShopPhoneNumberRepositoryImpl(ShopPhoneNumberJpaRepository shopPhoneNumberJpaRepository) {
        this.shopPhoneNumberJpaRepository = shopPhoneNumberJpaRepository;
    }

    @Override
    public ShopPhoneNumber save(ShopPhoneNumber shopPhoneNumber) {
        if (shopPhoneNumber.getId() == null) {
            ShopPhoneNumberJpaEntity saved = shopPhoneNumberJpaRepository.save(ShopPhoneNumberMapper.toEntity(shopPhoneNumber));
            return ShopPhoneNumberMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopPhoneNumberJpaEntity entity = shopPhoneNumberJpaRepository.findById(shopPhoneNumber.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 가게 전화번호입니다: " + shopPhoneNumber.getId()));
        ShopPhoneNumberMapper.applyChanges(entity, shopPhoneNumber);
        return ShopPhoneNumberMapper.toDomain(entity);
    }

    @Override
    public List<ShopPhoneNumber> findByShopId(Long shopId) {
        return shopPhoneNumberJpaRepository.findByShopId(ShopId.of(shopId)).stream()
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
