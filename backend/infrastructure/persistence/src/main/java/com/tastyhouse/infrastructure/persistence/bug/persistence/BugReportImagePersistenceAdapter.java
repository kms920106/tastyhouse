package com.tastyhouse.infrastructure.persistence.bug.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.application.bug.port.out.write.BugReportImagePersistencePort;

@Repository
class BugReportImagePersistenceAdapter implements BugReportImagePersistencePort {

    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    public BugReportImagePersistenceAdapter(BugReportImageJpaRepository bugReportImageJpaRepository) {
        this.bugReportImageJpaRepository = bugReportImageJpaRepository;
    }

    @Override
    public BugReportImage save(BugReportImage bugReportImage) {
        BugReportImageJpaEntity saved = bugReportImageJpaRepository.save(BugReportImageMapper.toEntity(bugReportImage));
        return BugReportImageMapper.toDomain(saved);
    }
}
