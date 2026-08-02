package com.tastyhouse.domain.review.domain.model;

import com.tastyhouse.domain.review.model.ReviewImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 불변 애그리거트 단위 테스트. of/reconstitute 왕복만 검증한다(상태전이 없음).
 */
class ReviewImageTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientImage() {
        ReviewImage image = ReviewImage.of(ReviewId.of(1L), UploadedFileId.of(100L), 1);

        assertThat(image.getId()).isNull();
        assertThat(image.getReviewId()).isEqualTo(ReviewId.of(1L));
        assertThat(image.getImageFileId()).isEqualTo(UploadedFileId.of(100L));
        assertThat(image.getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ReviewImage image = ReviewImage.reconstitute(9L, ReviewId.of(1L), UploadedFileId.of(100L), 2);

        assertThat(image.getId()).isEqualTo(9L);
        assertThat(image.getReviewId()).isEqualTo(ReviewId.of(1L));
        assertThat(image.getImageFileId()).isEqualTo(UploadedFileId.of(100L));
        assertThat(image.getSort()).isEqualTo(2);
    }
}
