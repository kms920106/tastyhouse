package com.tastyhouse.domain.banner.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.domain.file.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class BannerTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientBanner() {
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 0, 0);

        Banner banner = Banner.of(
            BannerType.HOME, "제목", UploadedFileId.of(1L), "https://example.com", startDate, endDate, 1, true);

        assertThat(banner.getId()).isNull();
        assertThat(banner.getType()).isEqualTo(BannerType.HOME);
        assertThat(banner.getTitle()).isEqualTo("제목");
        assertThat(banner.getImageFileId()).isEqualTo(UploadedFileId.of(1L));
        assertThat(banner.getLinkUrl()).isEqualTo("https://example.com");
        assertThat(banner.getStartDate()).isEqualTo(startDate);
        assertThat(banner.getEndDate()).isEqualTo(endDate);
        assertThat(banner.getSort()).isEqualTo(1);
        assertThat(banner.isVisible()).isTrue();
        assertThat(banner.isDeleted()).isFalse();
        assertThat(banner.getCreatedAt()).isNull();
        assertThat(banner.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 배너 필드 전체를 변경한다")
    void update_changesFields() {
        Banner banner = Banner.of(
            BannerType.HOME, "제목", UploadedFileId.of(1L), "https://example.com",
            LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 0, 0), 1, true
        );

        LocalDateTime newStartDate = LocalDateTime.of(2027, 1, 1, 0, 0);
        LocalDateTime newEndDate = LocalDateTime.of(2027, 6, 30, 0, 0);
        banner.update(BannerType.SIDEBAR, "새 제목", UploadedFileId.of(2L), "https://new.example.com", newStartDate, newEndDate, 2, false);

        assertThat(banner.getType()).isEqualTo(BannerType.SIDEBAR);
        assertThat(banner.getTitle()).isEqualTo("새 제목");
        assertThat(banner.getImageFileId()).isEqualTo(UploadedFileId.of(2L));
        assertThat(banner.getLinkUrl()).isEqualTo("https://new.example.com");
        assertThat(banner.getStartDate()).isEqualTo(newStartDate);
        assertThat(banner.getEndDate()).isEqualTo(newEndDate);
        assertThat(banner.getSort()).isEqualTo(2);
        assertThat(banner.isVisible()).isFalse();
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        Banner banner = Banner.of(
            BannerType.HOME, "제목", UploadedFileId.of(1L), "https://example.com",
            LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 0, 0), 1, true
        );

        banner.delete();

        assertThat(banner.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 0, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Banner banner = Banner.reconstitute(
            1L, BannerType.HOME, "제목", UploadedFileId.of(1L), "https://example.com",
            startDate, endDate, 1, true, false, createdAt, updatedAt
        );

        assertThat(banner.getId()).isEqualTo(1L);
        assertThat(banner.getBannerId()).isEqualTo(BannerId.of(1L));
        assertThat(banner.getCreatedAt()).isEqualTo(createdAt);
        assertThat(banner.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getBannerId를 호출하면 BannerId 불변식 위반으로 예외가 발생한다")
    void getBannerId_onTransient_throws() {
        Banner banner = Banner.of(
            BannerType.HOME, "제목", UploadedFileId.of(1L), "https://example.com",
            LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 0, 0), 1, true
        );

        assertThatThrownBy(banner::getBannerId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
