package com.tastyhouse.infrastructure.bug.persistence;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportImageRepository;

@Repository
@RequiredArgsConstructor
public class BugReportImageRepositoryImpl implements BugReportImageRepository {

    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    @Override
    public List<BugReportImage> findByBugReportId(Long bugReportId) {
        return bugReportImageJpaRepository.findByBugReportIdOrderBySortAsc(bugReportId).stream()
            .map(BugReportImageMapper::toDomain)
            .toList();
    }

    @Override
    public BugReportImage save(BugReportImage bugReportImage) {
        BugReportImageJpaEntity saved = bugReportImageJpaRepository.save(BugReportImageMapper.toEntity(bugReportImage));
        return BugReportImageMapper.toDomain(saved);
    }
}
