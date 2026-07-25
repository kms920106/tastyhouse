package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageChangeRequest;
import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.domain.shop.domain.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.core.shared.model.ApprovalStatus;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;

@Repository
@RequiredArgsConstructor
public class ShopImageChangeRequestRepositoryImpl implements ShopImageChangeRequestRepository {

    private final JPAQueryFactory queryFactory;
    private final ShopImageChangeRequestJpaRepository shopImageChangeRequestJpaRepository;

    @Override
    public ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest) {
        if (shopImageChangeRequest.getId() == null) {
            ShopImageChangeRequestJpaEntity saved =
                shopImageChangeRequestJpaRepository.save(ShopImageChangeRequestMapper.toEntity(shopImageChangeRequest));
            return ShopImageChangeRequestMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopImageChangeRequestJpaEntity entity = shopImageChangeRequestJpaRepository.findById(shopImageChangeRequest.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이미지 변경 요청입니다: " + shopImageChangeRequest.getId()));
        ShopImageChangeRequestMapper.applyChanges(entity, shopImageChangeRequest);
        return ShopImageChangeRequestMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopImageChangeRequest> findById(Long id) {
        return shopImageChangeRequestJpaRepository.findById(id)
            .map(ShopImageChangeRequestMapper::toDomain);
    }

    @Override
    public List<ShopImageChangeRequest> findByShopId(Long shopId) {
        return queryFactory
            .selectFrom(shopImageChangeRequestJpaEntity)
            .where(shopImageChangeRequestJpaEntity.shopId.eq(shopId))
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .fetch()
            .stream()
            .map(ShopImageChangeRequestMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status) {
        Integer result = queryFactory
            .selectOne()
            .from(shopImageChangeRequestJpaEntity)
            .where(
                shopImageChangeRequestJpaEntity.shopId.eq(shopId),
                imageTypeEq(imageType),
                statusEq(status)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status) {
        Integer result = queryFactory
            .selectOne()
            .from(shopImageChangeRequestJpaEntity)
            .where(
                shopImageChangeRequestJpaEntity.shopId.eq(shopId),
                statusEq(status)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public PageResult<ShopImageChangeRequest> findByStatusAndImageType(ApprovalStatus status, ShopImageType imageType, PageQuery pageQuery) {
        Long total = queryFactory
            .select(shopImageChangeRequestJpaEntity.id.count())
            .from(shopImageChangeRequestJpaEntity)
            .where(
                statusEq(status),
                imageTypeEq(imageType)
            )
            .fetchOne();

        List<ShopImageChangeRequest> items = queryFactory
            .selectFrom(shopImageChangeRequestJpaEntity)
            .where(
                statusEq(status),
                imageTypeEq(imageType)
            )
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(ShopImageChangeRequestMapper::toDomain)
            .toList();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression statusEq(ApprovalStatus status) {
        return status != null ? shopImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression imageTypeEq(ShopImageType imageType) {
        return imageType != null ? shopImageChangeRequestJpaEntity.imageType.eq(imageType) : null;
    }
}
