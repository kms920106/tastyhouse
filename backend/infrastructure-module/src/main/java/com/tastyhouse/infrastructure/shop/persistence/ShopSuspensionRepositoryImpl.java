package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;

@Repository
public class ShopSuspensionRepositoryImpl implements ShopSuspensionRepository {

    private final ShopSuspensionJpaRepository shopSuspensionJpaRepository;

    public ShopSuspensionRepositoryImpl(ShopSuspensionJpaRepository shopSuspensionJpaRepository) {
        this.shopSuspensionJpaRepository = shopSuspensionJpaRepository;
    }

    @Override
    public ShopSuspension save(ShopSuspension shopSuspension) {
        if (shopSuspension.getId() == null) {
            ShopSuspensionJpaEntity saved = shopSuspensionJpaRepository.save(ShopSuspensionMapper.toEntity(shopSuspension));
            return ShopSuspensionMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush한다.
        // detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopSuspensionJpaEntity entity = shopSuspensionJpaRepository.findById(shopSuspension.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 영업 임시중지입니다: " + shopSuspension.getId()));
        ShopSuspensionMapper.applyChanges(entity, shopSuspension);
        return ShopSuspensionMapper.toDomain(entity);
    }

    @Override
    public List<ShopSuspension> findByShopId(Long shopId) {
        return shopSuspensionJpaRepository.findByShopId(ShopId.of(shopId))
            .stream()
            .map(ShopSuspensionMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopSuspension> findById(Long id) {
        return shopSuspensionJpaRepository.findById(id).map(ShopSuspensionMapper::toDomain);
    }
}
