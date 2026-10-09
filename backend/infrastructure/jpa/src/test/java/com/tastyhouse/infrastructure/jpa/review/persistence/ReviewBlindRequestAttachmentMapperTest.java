package com.tastyhouse.infrastructure.jpa.review.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewBlindRequestAttachment;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewBlindRequestAttachmentMapperTest {

    @Test
    @DisplayName("ReviewBlindRequestAttachment → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewBlindRequestAttachmentJpaEntity entity = ReviewBlindRequestAttachmentMapper.toEntity(attachment());

        assertThat(entity.getBlindRequestId()).isEqualTo(182L);
        assertThat(entity.getAttachmentFileId()).isEqualTo(183L);
        assertThat(entity.getSort()).isEqualTo(4);
    }

    @Test
    @DisplayName("엔티티 → ReviewBlindRequestAttachment 변환 시 모든 필드가 보존된다")
    void toDomain() {
        ReviewBlindRequestAttachment original = attachment();
        ReviewBlindRequestAttachmentJpaEntity entity = ReviewBlindRequestAttachmentMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());

        ReviewBlindRequestAttachment restored = ReviewBlindRequestAttachmentMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static ReviewBlindRequestAttachment attachment() {
        return ReviewBlindRequestAttachment.reconstitute(
            181L, ReviewBlindRequestId.of(182L), UploadedFileId.of(183L), 4);
    }
}
