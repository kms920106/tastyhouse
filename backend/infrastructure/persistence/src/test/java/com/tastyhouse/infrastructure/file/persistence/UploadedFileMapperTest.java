package com.tastyhouse.infrastructure.file.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.model.UploadedFile;

import static org.assertj.core.api.Assertions.assertThat;

class UploadedFileMapperTest {

    @Test
    @DisplayName("UploadedFile → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        UploadedFileJpaEntity entity = UploadedFileMapper.toEntity(original());

        assertThat(entity.getOriginalFilename()).isEqualTo("원본.png");
        assertThat(entity.getStoredFilename()).isEqualTo("stored-uuid.png");
        assertThat(entity.getFilePath()).isEqualTo("images/2026/stored-uuid.png");
        assertThat(entity.getFileSize()).isEqualTo(20480L);
        assertThat(entity.getContentType()).isEqualTo("image/png");
    }

    @Test
    @DisplayName("엔티티 → UploadedFile 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        UploadedFile original = original();
        UploadedFileJpaEntity entity = UploadedFileMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        UploadedFile restored = UploadedFileMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static UploadedFile original() {
        return UploadedFile.reconstitute(
            11L, "원본.png", "stored-uuid.png", "images/2026/stored-uuid.png", 20480L, "image/png",
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 6, 7, 8, 9, 10));
    }
}
