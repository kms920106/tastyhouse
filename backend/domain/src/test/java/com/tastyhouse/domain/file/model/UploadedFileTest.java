package com.tastyhouse.domain.file.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadedFileTest {
    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사 시각 없음)다")
    void of_createsTransientUploadedFile() {
        UploadedFile uploadedFile = UploadedFile.of(
            "original.png", "stored-uuid.png", "/files/stored-uuid.png", 1024L, "image/png"
        );

        assertThat(uploadedFile.getId()).isNull();
        assertThat(uploadedFile.getOriginalFilename()).isEqualTo("original.png");
        assertThat(uploadedFile.getStoredFilename()).isEqualTo("stored-uuid.png");
        assertThat(uploadedFile.getFilePath()).isEqualTo("/files/stored-uuid.png");
        assertThat(uploadedFile.getFileSize()).isEqualTo(1024L);
        assertThat(uploadedFile.getContentType()).isEqualTo("image/png");
        assertThat(uploadedFile.getCreatedAt()).isNull();
        assertThat(uploadedFile.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사 시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime now = LocalDateTime.now();

        UploadedFile uploadedFile = UploadedFile.reconstitute(
            1L, "original.png", "stored-uuid.png", "/files/stored-uuid.png", 1024L, "image/png", now, now
        );

        assertThat(uploadedFile.getId()).isEqualTo(1L);
        assertThat(uploadedFile.getUploadedFileId()).isEqualTo(UploadedFileId.of(1L));
        assertThat(uploadedFile.getCreatedAt()).isEqualTo(now);
        assertThat(uploadedFile.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("미영속 상태에서 getUploadedFileId를 호출하면 UploadedFileId 불변식 위반으로 예외가 발생한다")
    void getUploadedFileId_onTransient_throws() {
        UploadedFile uploadedFile = UploadedFile.of(
            "original.png", "stored-uuid.png", "/files/stored-uuid.png", 1024L, "image/png"
        );

        assertThatThrownBy(uploadedFile::getUploadedFileId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
