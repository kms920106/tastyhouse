package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentBoard;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.domain.shop.domain.repository.ShopContentBoardRepository;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.shop.persistence.QShopContentBoardJpaEntity.shopContentBoardJpaEntity;

@Repository
@RequiredArgsConstructor
public class ShopContentBoardRepositoryImpl implements ShopContentBoardRepository {

    private final ShopContentBoardJpaRepository shopContentBoardJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public ShopContentBoard save(ShopContentBoard shopContentBoard) {
        if (shopContentBoard.getId() == null) {
            ShopContentBoardJpaEntity saved = shopContentBoardJpaRepository.save(ShopContentBoardMapper.toEntity(shopContentBoard));
            return ShopContentBoardMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopContentBoardJpaEntity entity = shopContentBoardJpaRepository.findById(shopContentBoard.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 콘텐츠보드입니다: " + shopContentBoard.getId()));
        ShopContentBoardMapper.applyChanges(entity, shopContentBoard);
        return ShopContentBoardMapper.toDomain(entity);
    }

    @Override
    public List<ShopContentBoard> findByShopId(Long shopId) {
        return shopContentBoardJpaRepository.findByShopId(shopId)
            .stream()
            .map(ShopContentBoardMapper::toDomain)
            .toList();
    }

    @Override
    public PageResult<ShopContentBoard> findAll(Long shopId, Boolean hidden, ShopContentType contentType, PageQuery pageQuery) {
        Long total = queryFactory
            .select(shopContentBoardJpaEntity.id.count())
            .from(shopContentBoardJpaEntity)
            .where(
                shopIdEq(shopId),
                hiddenEq(hidden),
                contentTypeEq(contentType)
            )
            .fetchOne();

        List<ShopContentBoard> items = queryFactory
            .selectFrom(shopContentBoardJpaEntity)
            .where(
                shopIdEq(shopId),
                hiddenEq(hidden),
                contentTypeEq(contentType)
            )
            .orderBy(shopContentBoardJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(ShopContentBoardMapper::toDomain)
            .toList();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ShopContentBoard> findById(Long id) {
        return shopContentBoardJpaRepository.findById(id).map(ShopContentBoardMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopContentBoardJpaRepository.deleteById(id);
    }

    @Override
    public long countByShopId(Long shopId) {
        return shopContentBoardJpaRepository.countByShopId(shopId);
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? shopContentBoardJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression hiddenEq(Boolean hidden) {
        return hidden != null ? shopContentBoardJpaEntity.hidden.eq(hidden) : null;
    }

    private BooleanExpression contentTypeEq(ShopContentType contentType) {
        return contentType != null ? shopContentBoardJpaEntity.contentType.eq(contentType) : null;
    }
}
