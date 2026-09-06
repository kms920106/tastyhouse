package com.tastyhouse.application.grade.port.out;

public record GradeInfoResult(
    String grade,
    String displayName,
    int minReviewCount,
    Integer maxReviewCount
) {
}
