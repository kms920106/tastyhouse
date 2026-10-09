package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopContentBoardJpaEntity.shopContentBoardJpaEntity;

@Repository
class ShopContentBoardPersistenceAdapter implements ShopContentBoardLoadPort, ShopContentBoardSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopContentBoardJpaRepository shopContentBoardJpaRepository;

    public ShopContentBoardPersistenceAdapter(JPAQueryFactory queryFactory, ShopContentBoardJpaRepository shopContentBoardJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopContentBoardJpaRepository = shopContentBoardJpaRepository;
    }

    @Override
    public ShopContentBoard save(ShopContentBoard shopContentBoard) {
        if (shopContentBoard.getId() == null) {
            ShopContentBoardJpaEntity saved = shopContentBoardJpaRepository.save(ShopContentBoardMapper.toEntity(shopContentBoard));
            return ShopContentBoardMapper.toDomain(saved);
        }

        ShopContentBoardJpaEntity entity = shopContentBoardJpaRepository.findById(shopContentBoard.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 콘텐츠보드입니다: " + shopContentBoard.getId()));
        ShopContentBoardMapper.applyChanges(entity, shopContentBoard);
        return ShopContentBoardMapper.toDomain(entity);
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
        Long count = queryFactory
            .select(shopContentBoardJpaEntity.count())
            .from(shopContentBoardJpaEntity)
            .where(shopContentBoardJpaEntity.shopId.eq(shopId))
            .fetchOne();
        return count == null ? 0L : count;
    }
}
