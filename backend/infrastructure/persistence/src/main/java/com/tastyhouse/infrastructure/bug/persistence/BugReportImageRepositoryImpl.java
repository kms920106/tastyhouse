package com.tastyhouse.infrastructure.bug.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.repository.BugReportImageRepository;

@Repository
public class BugReportImageRepositoryImpl implements BugReportImageRepository {
    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    public BugReportImageRepositoryImpl(BugReportImageJpaRepository bugReportImageJpaRepository) {
        this.bugReportImageJpaRepository = bugReportImageJpaRepository;
    }

    @Override
    public BugReportImage save(BugReportImage bugReportImage) {
        BugReportImageJpaEntity saved = bugReportImageJpaRepository.save(BugReportImageMapper.toEntity(bugReportImage));
        return BugReportImageMapper.toDomain(saved);
    }
}
