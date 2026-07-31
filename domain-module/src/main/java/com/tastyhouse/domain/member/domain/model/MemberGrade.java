package com.tastyhouse.domain.member.domain.model;

import java.util.Arrays;
import java.util.Comparator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum MemberGrade {
    NEWCOMER(1, "신입멤버", 0),
    ACTIVE(2, "열심멤버", 100),
    INSIDER(3, "인싸멤버", 500),
    GOURMET(4, "미식멤버", 700),
    TEHA(5, "테하멤버", 1000);

    private final int level;
    private final String displayName;
    private final int minReviewCount;

    public static MemberGrade from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.MEMBER_GRADE_TYPE_UNKNOWN,
                ErrorCode.MEMBER_GRADE_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public static MemberGrade fromReviewCount(int reviewCount) {
        return Arrays.stream(values())
            .filter(grade -> reviewCount >= grade.minReviewCount)
            .max(Comparator.comparingInt(grade -> grade.minReviewCount))
            .orElse(NEWCOMER);
    }

    public static MemberGrade fromLevel(int level) {
        return Arrays.stream(values())
            .filter(grade -> grade.level == level)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid member grade level: " + level));
    }

    public Integer getMaxReviewCount() {
        if (this.isHigherThanOrEqual(TEHA)) {
            return null;
        }
        return fromLevel(this.level + 1).minReviewCount - 1;
    }

    public boolean isHigherThanOrEqual(MemberGrade other) {
        return this.level >= other.level;
    }
}
