package com.tastyhouse.application.review.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.application.review.port.out.ReviewSortSpec;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewSortSpecsTest {

    @Test
    @DisplayName("추천순은 좋아요 수 내림차순 후 최신순, 최신순·오래된순은 작성 시각 방향만 다르다")
    void sortSpecs() {
        assertThat(ReviewSortSpecs.of(ReviewSortType.RECOMMENDED)).isEqualTo(new ReviewSortSpec(true, false));
        assertThat(ReviewSortSpecs.of(ReviewSortType.LATEST)).isEqualTo(new ReviewSortSpec(false, false));
        assertThat(ReviewSortSpecs.of(ReviewSortType.OLDEST)).isEqualTo(new ReviewSortSpec(false, true));
    }
}
