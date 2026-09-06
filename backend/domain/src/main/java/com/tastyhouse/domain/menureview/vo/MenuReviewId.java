package com.tastyhouse.domain.menureview.vo;

/**
 * 메뉴 평가 식별자.
 */
public record MenuReviewId(Long value) {

    public MenuReviewId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MenuReviewId는 양수여야 합니다: " + value);
        }
    }

    public static MenuReviewId of(Long value) {
        return new MenuReviewId(value);
    }
}
