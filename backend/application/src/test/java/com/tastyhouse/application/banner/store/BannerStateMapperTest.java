package com.tastyhouse.application.banner.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.file.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;

class BannerStateMapperTest {

    @Test
    @DisplayName("Banner → BannerState → Banner 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        Banner original = Banner.reconstitute(
            11L, BannerType.SIDEBAR, "배너 제목", UploadedFileId.of(22L), "https://link",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            5, true, false,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0));

        Banner restored = BannerStateMapper.toDomain(BannerStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("boolean 필드와 nullable 필드가 뒤바뀌지 않는다")
    void booleanAndNullableFields() {
        Banner original = Banner.reconstitute(
            12L, BannerType.HOME, null, null, null, null, null, 1, false, true, null, null);

        Banner restored = BannerStateMapper.toDomain(BannerStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
