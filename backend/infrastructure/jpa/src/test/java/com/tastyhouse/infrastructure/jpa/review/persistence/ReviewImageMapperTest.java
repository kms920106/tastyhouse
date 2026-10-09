package com.tastyhouse.infrastructure.jpa.review.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewImageMapperTest {

    @Test
    @DisplayName("ReviewImage → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewImage original = ReviewImage.reconstitute(111L, ReviewId.of(112L), UploadedFileId.of(113L), 2);

        ReviewImageJpaEntity entity = ReviewImageMapper.toEntity(original);

        assertThat(entity.getReviewId()).isEqualTo(112L);
        assertThat(entity.getImageFileId()).isEqualTo(113L);
        assertThat(entity.getSort()).isEqualTo(2);
    }
}
