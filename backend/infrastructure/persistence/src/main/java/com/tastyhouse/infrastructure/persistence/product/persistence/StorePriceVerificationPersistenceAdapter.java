package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QStorePriceVerificationItemJpaEntity.storePriceVerificationItemJpaEntity;
import static com.tastyhouse.infrastructure.persistence.product.persistence.QStorePriceVerificationJpaEntity.storePriceVerificationJpaEntity;

@Repository
class StorePriceVerificationPersistenceAdapter implements StorePriceVerificationPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final StorePriceVerificationJpaRepository storePriceVerificationJpaRepository;
    private final StorePriceVerificationItemJpaRepository storePriceVerificationItemJpaRepository;

    public StorePriceVerificationPersistenceAdapter(
        JPAQueryFactory queryFactory,
        StorePriceVerificationJpaRepository storePriceVerificationJpaRepository,
        StorePriceVerificationItemJpaRepository storePriceVerificationItemJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.storePriceVerificationJpaRepository = storePriceVerificationJpaRepository;
        this.storePriceVerificationItemJpaRepository = storePriceVerificationItemJpaRepository;
    }

    @Override
    public StorePriceVerification save(StorePriceVerification verification) {
        if (verification.getId() == null) {
            StorePriceVerificationJpaEntity saved = storePriceVerificationJpaRepository
                .save(StorePriceVerificationMapper.toEntity(verification));
            return StorePriceVerificationMapper.toDomain(saved);
        }

        StorePriceVerificationJpaEntity entity = storePriceVerificationJpaRepository
            .findById(verification.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 매장 가격 인증 요청입니다: " + verification.getId()));
        StorePriceVerificationMapper.applyChanges(entity, verification);
        return StorePriceVerificationMapper.toDomain(entity);
    }

    @Override
    public Optional<StorePriceVerification> findById(StorePriceVerificationId id) {
        return storePriceVerificationJpaRepository.findById(id.value())
            .map(StorePriceVerificationMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(ShopId shopId, List<StorePriceVerificationStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return false;
        }
        return queryFactory
            .selectOne()
            .from(storePriceVerificationJpaEntity)
            .where(
                storePriceVerificationJpaEntity.shopId.eq(shopId.value()),
                storePriceVerificationJpaEntity.status.in(
                    statuses.stream().map(StorePriceVerificationStatus::name).toList()
                )
            )
            .fetchFirst() != null;
    }

    @Override
    public void saveItem(StorePriceVerificationItem item) {
        storePriceVerificationItemJpaRepository.save(StorePriceVerificationItemMapper.toEntity(item));
    }

    @Override
    public List<StorePriceVerificationItem> findAllItemsByVerificationId(StorePriceVerificationId verificationId) {
        return queryFactory
            .selectFrom(storePriceVerificationItemJpaEntity)
            .where(storePriceVerificationItemJpaEntity.verificationId.eq(verificationId.value()))
            .orderBy(storePriceVerificationItemJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(StorePriceVerificationItemMapper::toDomain)
            .toList();
    }
}
