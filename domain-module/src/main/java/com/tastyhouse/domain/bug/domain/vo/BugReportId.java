package com.tastyhouse.domain.bug.domain.vo;

public record BugReportId(Long value) {

    public BugReportId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("BugReportId는 양수여야 합니다: " + value);
        }
    }

    public static BugReportId of(Long value) {
        return new BugReportId(value);
    }
}
