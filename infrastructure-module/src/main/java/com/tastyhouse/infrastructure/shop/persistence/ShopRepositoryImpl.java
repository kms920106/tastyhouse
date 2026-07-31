package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * 가게 write 어댑터.
 *
 * <p>목록·검색·베스트·즐겨찾기 등 표현 목적 read는 같은 모듈의
 * {@link com.tastyhouse.infrastructure.shop.query.ShopSearchQueryDao}로 이관했다(공통 지침 패턴 4).
 */
@Repository
@RequiredArgsConstructor
public class ShopRepositoryImpl implements ShopRepository {

    private final JPAQueryFactory queryFactory;
    private final ShopJpaRepository shopJpaRepository;

    @Override
    public Optional<Shop> findById(ShopId id) {
        return shopJpaRepository.findById(id.value()).map(ShopMapper::toDomain);
    }

    @Override
    public Optional<Shop> findVisibleById(ShopId id) {
        ShopJpaEntity entity = queryFactory.selectFrom(shopJpaEntity)
            .where(
                shopJpaEntity.id.eq(id.value()),
                shopJpaEntity.permanentlyClosed.eq(false),
                shopJpaEntity.hidden.eq(false)
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopMapper::toDomain);
    }

    @Override
    public Shop save(Shop shop) {
        if (shop.getId() == null) {
            ShopJpaEntity saved = shopJpaRepository.save(ShopMapper.toEntity(shop));
            return ShopMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopJpaEntity entity = shopJpaRepository.findById(shop.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상점입니다: " + shop.getId()));
        ShopMapper.applyChanges(entity, shop);
        return ShopMapper.toDomain(entity);
    }
}
