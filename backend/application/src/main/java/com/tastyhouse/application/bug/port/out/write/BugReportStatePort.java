package com.tastyhouse.application.bug.port.out.write;

import java.util.Optional;

public interface BugReportStatePort {
    Optional<BugReportState> findById(Long id);

    BugReportState save(BugReportState state);
}
