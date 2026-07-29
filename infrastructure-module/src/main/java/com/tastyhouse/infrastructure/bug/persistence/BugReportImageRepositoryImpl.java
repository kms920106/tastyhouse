package com.tastyhouse.infrastructure.bug.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportImageRepository;

/**
 * 버그 제보 첨부 이미지 write 어댑터.
 *
 * <p>write 포트 순수화(공통 지침 패턴 4)에 따라 저장만 담당한다. 상세 화면의 첨부 목록 조회는 같은
 * 모듈의 {@code bug/query/BugReportQueryDao}가 파일 ID만 투영해 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class BugReportImageRepositoryImpl implements BugReportImageRepository {

    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    @Override
    public BugReportImage save(BugReportImage bugReportImage) {
        BugReportImageJpaEntity saved = bugReportImageJpaRepository.save(BugReportImageMapper.toEntity(bugReportImage));
        return BugReportImageMapper.toDomain(saved);
    }
}
