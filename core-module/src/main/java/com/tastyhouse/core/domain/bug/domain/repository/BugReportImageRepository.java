package com.tastyhouse.core.domain.bug.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;

public interface BugReportImageRepository {

    List<BugReportImage> findByBugReportId(Long bugReportId);

    BugReportImage save(BugReportImage bugReportImage);
}
