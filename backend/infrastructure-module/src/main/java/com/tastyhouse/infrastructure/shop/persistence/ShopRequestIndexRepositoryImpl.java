package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;

/**
 * 요청 인덱스 write 어댑터.
 *
 * <p>표현 목적 조회는 전부 {@code ShopRequestQueryDao}가 담당하므로 여기에는 상태 전이 경로에서 도메인
 * 모델을 로드하는 조회 2개만 있다.
 */
@Repository
public class ShopRequestIndexRepositoryImpl implements ShopRequestIndexRepository {

    private final ShopRequestIndexJpaRepository shopRequestIndexJpaRepository;

    public ShopRequestIndexRepositoryImpl(ShopRequestIndexJpaRepository shopRequestIndexJpaRepository) {
        this.shopRequestIndexJpaRepository = shopRequestIndexJpaRepository;
    }

    @Override
    public ShopRequestIndex save(ShopRequestIndex shopRequestIndex) {
        if (shopRequestIndex.getId() == null) {
            ShopRequestIndexJpaEntity saved =
                shopRequestIndexJpaRepository.save(ShopRequestIndexMapper.toEntity(shopRequestIndex));
            return ShopRequestIndexMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopRequestIndexJpaEntity entity = shopRequestIndexJpaRepository.findById(shopRequestIndex.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 요청 인덱스입니다: " + shopRequestIndex.getId()));
        ShopRequestIndexMapper.applyChanges(entity, shopRequestIndex);
        return ShopRequestIndexMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopRequestIndex> findById(Long id) {
        return shopRequestIndexJpaRepository.findById(id)
            .map(ShopRequestIndexMapper::toDomain);
    }

    @Override
    public Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(
        ShopRequestType requestType,
        Long sourceRequestId
    ) {
        return shopRequestIndexJpaRepository.findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .map(ShopRequestIndexMapper::toDomain);
    }
}
