package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportImageRepository;

@Repository
@RequiredArgsConstructor
public class BugReportImageRepositoryImpl implements BugReportImageRepository {

    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    @Override
    public BugReportImage save(BugReportImage bugReportImage) {
        return bugReportImageJpaRepository.save(bugReportImage);
    }
}
