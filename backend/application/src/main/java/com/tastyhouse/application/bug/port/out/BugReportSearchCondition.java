package com.tastyhouse.application.bug.port.out;

public record BugReportSearchCondition(
    String title,
    String content,
    Long memberId,
    String status,
    String category,
    String priority
) {

    public static BugReportSearchCondition of(
        String title,
        String content,
        Long memberId,
        String status,
        String category,
        String priority
    ) {
        return new BugReportSearchCondition(title, content, memberId, status, category, priority);
    }
}
