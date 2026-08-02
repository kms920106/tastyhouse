package com.tastyhouse.domain.review.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewSortTypeTest {

    @Test
    @DisplayName("from은 지원하는 정렬값을 enum으로 승격한다")
    void from_promotesSupportedCodes() {
        assertThat(ReviewSortType.from("RECOMMENDED")).isEqualTo(ReviewSortType.RECOMMENDED);
        assertThat(ReviewSortType.from("LATEST")).isEqualTo(ReviewSortType.LATEST);
        assertThat(ReviewSortType.from("OLDEST")).isEqualTo(ReviewSortType.OLDEST);
    }

    @Test
    @DisplayName("미지원 정렬값은 400 BusinessException으로 거절한다")
    void from_rejectsUnknownCode() {
        assertThatThrownBy(() -> ReviewSortType.from("POPULAR"))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> {
                BusinessException exception = (BusinessException) e;
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_SORT_TYPE_UNKNOWN);
                assertThat(exception.getErrorCode().getHttpStatusCode()).isEqualTo(400);
            });
    }
}
