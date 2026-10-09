package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeSavePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopOrderNoticeJpaEntity.shopOrderNoticeJpaEntity;

@Repository
class ShopOrderNoticePersistenceAdapter implements ShopOrderNoticeLoadPort, ShopOrderNoticeSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository;

    public ShopOrderNoticePersistenceAdapter(JPAQueryFactory queryFactory, ShopOrderNoticeJpaRepository shopOrderNoticeJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopOrderNoticeJpaRepository = shopOrderNoticeJpaRepository;
    }

    @Override
    public ShopOrderNotice save(ShopOrderNotice shopOrderNotice) {
        Long id = shopOrderNotice.getId() == null ? null : shopOrderNotice.getId().value();
        if (id == null) {
            ShopOrderNoticeJpaEntity saved =
                shopOrderNoticeJpaRepository.save(ShopOrderNoticeMapper.toEntity(shopOrderNotice));
            return ShopOrderNoticeMapper.toDomain(saved);
        }

        ShopOrderNoticeJpaEntity entity = shopOrderNoticeJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문안내입니다: " + id));
        ShopOrderNoticeMapper.applyChanges(entity, shopOrderNotice);
        return ShopOrderNoticeMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopOrderNotice> findByShopId(ShopId shopId) {
        ShopOrderNoticeJpaEntity entity = queryFactory
            .selectFrom(shopOrderNoticeJpaEntity)
            .where(shopOrderNoticeJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopOrderNoticeMapper::toDomain);
    }
}
