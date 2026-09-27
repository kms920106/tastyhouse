package com.tastyhouse.application.file.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.model.UploadedFile;

import static org.assertj.core.api.Assertions.assertThat;

class UploadedFileStateMapperTest {

    @Test
    @DisplayName("UploadedFile → UploadedFileState → UploadedFile 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        UploadedFile original = UploadedFile.reconstitute(
            11L, "원본.png", "stored-uuid.png", "images/2026/stored-uuid.png", 20480L, "image/png",
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 6, 7, 8, 9, 10));

        UploadedFile restored = UploadedFileStateMapper.toDomain(UploadedFileStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
