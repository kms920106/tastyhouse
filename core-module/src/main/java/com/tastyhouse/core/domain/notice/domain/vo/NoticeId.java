package com.tastyhouse.core.domain.notice.domain.vo;

public record NoticeId(Long value) {

    public NoticeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("NoticeId는 양수여야 합니다: " + value);
        }
    }

    public static NoticeId of(Long value) {
        return new NoticeId(value);
    }
}
