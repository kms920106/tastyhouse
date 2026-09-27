package com.tastyhouse.infrastructure.bug.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.file.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;

class BugReportImageMapperTest {

    @Test
    @DisplayName("BugReportImage → 엔티티 변환 시 VO는 value로 컬럼에 채워진다")
    void toEntity() {
        BugReportImage image = BugReportImage.reconstitute(7L, BugReportId.of(8L), UploadedFileId.of(9L), 2);

        BugReportImageJpaEntity entity = BugReportImageMapper.toEntity(image);

        assertThat(entity.getBugReportId()).isEqualTo(8L);
        assertThat(entity.getImageFileId()).isEqualTo(9L);
        assertThat(entity.getSort()).isEqualTo(2);
    }

    @Test
    @DisplayName("엔티티 → BugReportImage 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomain() {
        BugReportImageJpaEntity entity = BugReportImageJpaEntity.create(8L, 9L, 2);
        ReflectionTestUtils.setField(entity, "id", 7L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 2, 0, 0));

        BugReportImage image = BugReportImageMapper.toDomain(entity);

        assertThat(image).usingRecursiveComparison()
            .isEqualTo(BugReportImage.reconstitute(7L, BugReportId.of(8L), UploadedFileId.of(9L), 2));
    }
}
