package com.tastyhouse.infrastructure.ceo.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;

/**
 * 점주 로그인 이력 write 어댑터.
 *
 * <p>append-only라 insert 경로만 있다 — 다른 어댑터의 {@code save}가 갖는 "id가 있으면 managed 엔티티를
 * 찾아 필드 복사" update 분기가 필요 없다.
 */
@Repository
public class CeoLoginHistoryRepositoryImpl implements CeoLoginHistoryRepository {

    private final CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository;

    public CeoLoginHistoryRepositoryImpl(CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository) {
        this.ceoLoginHistoryJpaRepository = ceoLoginHistoryJpaRepository;
    }

    @Override
    public CeoLoginHistory save(CeoLoginHistory ceoLoginHistory) {
        CeoLoginHistoryJpaEntity saved = ceoLoginHistoryJpaRepository
            .save(CeoLoginHistoryMapper.toEntity(ceoLoginHistory));
        return CeoLoginHistoryMapper.toDomain(saved);
    }
}
