package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeSavePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopNoticeJpaEntity.shopNoticeJpaEntity;

@Repository
class ShopNoticePersistenceAdapter implements ShopNoticeLoadPort, ShopNoticeSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopNoticeJpaRepository shopNoticeJpaRepository;

    public ShopNoticePersistenceAdapter(JPAQueryFactory queryFactory, ShopNoticeJpaRepository shopNoticeJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopNoticeJpaRepository = shopNoticeJpaRepository;
    }

    @Override
    public ShopNotice save(ShopNotice shopNotice) {
        if (shopNotice.getId() == null) {
            ShopNoticeJpaEntity saved = shopNoticeJpaRepository.save(ShopNoticeMapper.toEntity(shopNotice));
            return ShopNoticeMapper.toDomain(saved);
        }

        ShopNoticeJpaEntity entity = shopNoticeJpaRepository.findById(shopNotice.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 점주 공지입니다: " + shopNotice.getId()));
        ShopNoticeMapper.applyChanges(entity, shopNotice);
        return ShopNoticeMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopNotice> findById(Long id) {
        return shopNoticeJpaRepository.findById(id).map(ShopNoticeMapper::toDomain);
    }

    @Override
    public Optional<ShopNotice> findExposedByShopId(ShopId shopId) {
        ShopNoticeJpaEntity entity = queryFactory
            .selectFrom(shopNoticeJpaEntity)
            .where(
                shopNoticeJpaEntity.shopId.eq(shopId.value()),
                shopNoticeJpaEntity.exposed.isTrue()
            )
            .orderBy(shopNoticeJpaEntity.id.desc())
            .fetchFirst();
        return Optional.ofNullable(entity).map(ShopNoticeMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopNoticeJpaRepository.deleteById(id);
    }
}
