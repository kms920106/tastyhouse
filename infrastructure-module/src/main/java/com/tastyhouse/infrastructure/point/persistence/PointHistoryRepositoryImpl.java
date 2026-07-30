package com.tastyhouse.infrastructure.point.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.point.domain.model.PointHistory;
import com.tastyhouse.core.domain.point.domain.repository.PointHistoryRepository;

/**
 * 포인트 변동 이력 write 어댑터.
 *
 * <p>이력은 insert 전용이므로 저장만 담당한다(update 경로가 없어 load-copy-save 불필요). 표현 목적
 * 조회(전체 목록·페이징·유형 필터)는 같은 모듈의 {@code PointQueryDao}로 이관했다(공통 지침 패턴 3·4).
 */
@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public PointHistory save(PointHistory history) {
        PointHistoryJpaEntity saved = pointHistoryJpaRepository.save(PointHistoryMapper.toEntity(history));
        return PointHistoryMapper.toDomain(saved);
    }
}
