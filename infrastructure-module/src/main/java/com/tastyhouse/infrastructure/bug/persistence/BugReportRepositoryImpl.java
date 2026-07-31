package com.tastyhouse.infrastructure.bug.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.bug.domain.model.BugReport;
import com.tastyhouse.domain.bug.domain.repository.BugReportRepository;
import com.tastyhouse.domain.bug.domain.vo.BugReportId;

/**
 * 버그 제보 write 어댑터.
 *
 * <p>write 포트 순수화(공통 지침 패턴 4)에 따라 단건 로드·저장만 담당한다. 목록/검색 등 표현 목적
 * 조회는 같은 모듈의 {@code bug/query/BugReportQueryDao}로 이관되어, 이 클래스는 QueryDSL을 쓰지 않는다.
 */
@Repository
@RequiredArgsConstructor
public class BugReportRepositoryImpl implements BugReportRepository {

    private final BugReportJpaRepository bugReportJpaRepository;

    @Override
    public Optional<BugReport> findById(BugReportId bugReportId) {
        if (bugReportId == null) {
            return Optional.empty();
        }
        return bugReportJpaRepository.findById(bugReportId.value())
            .map(BugReportMapper::toDomain);
    }

    @Override
    public BugReport save(BugReport bugReport) {
        if (bugReport.getId() == null) {
            BugReportJpaEntity saved = bugReportJpaRepository.save(BugReportMapper.toEntity(bugReport));
            return BugReportMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        BugReportJpaEntity entity = bugReportJpaRepository.findById(bugReport.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 버그 신고입니다: " + bugReport.getId()));
        BugReportMapper.applyChanges(entity, bugReport);
        return BugReportMapper.toDomain(entity);
    }
}
