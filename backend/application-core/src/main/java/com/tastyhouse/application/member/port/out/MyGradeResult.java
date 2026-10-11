package com.tastyhouse.application.member.port.out;

public record MyGradeResult(
    String currentGrade,
    String currentGradeDisplayName,
    String nextGrade,
    String nextGradeDisplayName,
    int currentReviewCount,
    int reviewsNeededForNextGrade
) {
}
