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

/**
 * 매장 가격 인증 요청 write 어댑터. 요청 본체와 항목이 같은 애그리거트 경계에서 함께 저장·조회되므로
 * 한 포트(한 구현)가 두 JPA 리포지토리를 감싼다. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
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
        // 항목은 접수 시 한 번 저장되고 이후 변경되지 않으므로 update 분기를 두지 않는다.
        // 저장 후 별도 조회 경로(findAllItemsByVerificationId)가 있어 저장 결과를 반환하지 않는다.
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
