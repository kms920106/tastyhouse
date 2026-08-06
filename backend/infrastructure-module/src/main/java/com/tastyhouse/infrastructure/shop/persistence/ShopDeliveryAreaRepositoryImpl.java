package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달가능지역 write 어댑터.
 *
 * <p>단건 로드·중복 검증·건수 카운트·저장·삭제만 담당한다. 행정동 이름을 조인해 표시용으로 완성하는
 * 목록 조회는 같은 모듈의 {@code shop/query/ShopDeliveryAreaQueryDao}가 담당한다(CQRS 분리).
 */
@Repository
public class ShopDeliveryAreaRepositoryImpl implements ShopDeliveryAreaRepository {

    private final ShopDeliveryAreaJpaRepository shopDeliveryAreaJpaRepository;

    public ShopDeliveryAreaRepositoryImpl(ShopDeliveryAreaJpaRepository shopDeliveryAreaJpaRepository) {
        this.shopDeliveryAreaJpaRepository = shopDeliveryAreaJpaRepository;
    }

    @Override
    public List<ShopDeliveryArea> findByShopId(ShopId shopId) {
        return shopDeliveryAreaJpaRepository.findByShopIdOrderByIdAsc(shopId.value()).stream()
            .map(ShopDeliveryAreaMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopDeliveryArea> findById(Long deliveryAreaId) {
        return shopDeliveryAreaJpaRepository.findById(deliveryAreaId)
            .map(ShopDeliveryAreaMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
        return shopDeliveryAreaJpaRepository.existsByShopIdAndAdminDongId(shopId.value(), adminDongId.value());
    }

    @Override
    public long countByShopId(ShopId shopId) {
        return shopDeliveryAreaJpaRepository.countByShopId(shopId.value());
    }

    @Override
    public ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea) {
        ShopDeliveryAreaJpaEntity saved = shopDeliveryAreaJpaRepository.save(ShopDeliveryAreaMapper.toEntity(shopDeliveryArea));
        return ShopDeliveryAreaMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long deliveryAreaId) {
        shopDeliveryAreaJpaRepository.deleteById(deliveryAreaId);
    }
}
