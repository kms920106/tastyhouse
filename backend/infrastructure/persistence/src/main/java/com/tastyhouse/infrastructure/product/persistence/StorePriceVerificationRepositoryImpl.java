package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.repository.StorePriceVerificationRepository;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class StorePriceVerificationRepositoryImpl implements StorePriceVerificationRepository {
    private final StorePriceVerificationJpaRepository storePriceVerificationJpaRepository;
    private final StorePriceVerificationItemJpaRepository storePriceVerificationItemJpaRepository;

    public StorePriceVerificationRepositoryImpl(
        StorePriceVerificationJpaRepository storePriceVerificationJpaRepository,
        StorePriceVerificationItemJpaRepository storePriceVerificationItemJpaRepository
    ) {
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
    public Optional<StorePriceVerification> findLatestByShopId(ShopId shopId) {
        return storePriceVerificationJpaRepository.findFirstByShopIdOrderByIdDesc(shopId.value())
            .map(StorePriceVerificationMapper::toDomain);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(ShopId shopId, List<StorePriceVerificationStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return false;
        }
        return storePriceVerificationJpaRepository.existsByShopIdAndStatusIn(shopId.value(), statuses);
    }

    @Override
    public void saveItem(StorePriceVerificationItem item) {
        storePriceVerificationItemJpaRepository.save(StorePriceVerificationItemMapper.toEntity(item));
    }

    @Override
    public List<StorePriceVerificationItem> findAllItemsByVerificationId(StorePriceVerificationId verificationId) {
        return storePriceVerificationItemJpaRepository
            .findAllByVerificationIdOrderByIdAsc(verificationId.value()).stream()
            .map(StorePriceVerificationItemMapper::toDomain)
            .toList();
    }
}
