package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.StorePriceVerificationItemState;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationState;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationStatePort;

@Repository
public class StorePriceVerificationStatePortImpl implements StorePriceVerificationStatePort {
    private final StorePriceVerificationJpaRepository storePriceVerificationJpaRepository;
    private final StorePriceVerificationItemJpaRepository storePriceVerificationItemJpaRepository;

    public StorePriceVerificationStatePortImpl(
        StorePriceVerificationJpaRepository storePriceVerificationJpaRepository,
        StorePriceVerificationItemJpaRepository storePriceVerificationItemJpaRepository
    ) {
        this.storePriceVerificationJpaRepository = storePriceVerificationJpaRepository;
        this.storePriceVerificationItemJpaRepository = storePriceVerificationItemJpaRepository;
    }

    @Override
    public StorePriceVerificationState save(StorePriceVerificationState state) {
        if (state.id() == null) {
            StorePriceVerificationJpaEntity saved = storePriceVerificationJpaRepository
                .save(StorePriceVerificationMapper.toEntity(state));
            return StorePriceVerificationMapper.toState(saved);
        }

        StorePriceVerificationJpaEntity entity = storePriceVerificationJpaRepository
            .findById(state.id())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 매장 가격 인증 요청입니다: " + state.id()));
        StorePriceVerificationMapper.applyChanges(entity, state);
        return StorePriceVerificationMapper.toState(entity);
    }

    @Override
    public Optional<StorePriceVerificationState> findById(Long id) {
        return storePriceVerificationJpaRepository.findById(id)
            .map(StorePriceVerificationMapper::toState);
    }

    @Override
    public Optional<StorePriceVerificationState> findLatestByShopId(Long shopId) {
        return storePriceVerificationJpaRepository.findFirstByShopIdOrderByIdDesc(shopId)
            .map(StorePriceVerificationMapper::toState);
    }

    @Override
    public boolean existsByShopIdAndStatusIn(Long shopId, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return false;
        }
        return storePriceVerificationJpaRepository.existsByShopIdAndStatusIn(shopId, statuses);
    }

    @Override
    public void saveItem(StorePriceVerificationItemState item) {
        storePriceVerificationItemJpaRepository.save(StorePriceVerificationItemMapper.toEntity(item));
    }

    @Override
    public List<StorePriceVerificationItemState> findAllItemsByVerificationId(Long verificationId) {
        return storePriceVerificationItemJpaRepository
            .findAllByVerificationIdOrderByIdAsc(verificationId).stream()
            .map(StorePriceVerificationItemMapper::toState)
            .toList();
    }
}
