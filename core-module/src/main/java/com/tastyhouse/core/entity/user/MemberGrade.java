package com.tastyhouse.core.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 회원 등급
 * - 활동량, 리뷰 수 등에 따라 결정되는 회원의 등급
 */
@Getter
@RequiredArgsConstructor
public enum MemberGrade {
    NEWCOMER(1, "신입멤버", 0), // 가장 낮은 등급
    ACTIVE(2, "열심멤버", 100),
    INSIDER(3, "인싸멤버", 500),
    GOURMET(4, "미식멤버", 700),
    TEHA(5, "테하멤버", 1000); // 가장 높은 등급

    private final int level;
    private final String displayName;
    private final int minReviewCount;

    /**
     * 리뷰 개수로 등급을 결정합니다
     */
    public static MemberGrade fromReviewCount(int reviewCount) {
        return Arrays.stream(values())
            .filter(grade -> reviewCount >= grade.minReviewCount)
            .max(Comparator.comparingInt(grade -> grade.minReviewCount))
            .orElse(NEWCOMER);
    }

    /**
     * level 값으로 MemberGrade를 찾습니다
     */
    public static MemberGrade fromLevel(int level) {
        return Arrays.stream(values())
            .filter(grade -> grade.level == level)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid member grade level: " + level));
    }

    /**
     * 다음 등급의 최소 리뷰 수 - 1 (해당 등급의 최대 리뷰 수)
     * 최고 등급(TEHA)은 null 반환
     */
    public Integer getMaxReviewCount() {
        if (this.isHigherThanOrEqual(TEHA)) {
            return null;
        }
        return fromLevel(this.level + 1).minReviewCount - 1;
    }

    /**
     * 다른 등급과 같거나 높은지 확인
     */
    public boolean isHigherThanOrEqual(MemberGrade other) {
        return this.level >= other.level;
    }
}
