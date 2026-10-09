package com.tastyhouse.infrastructure.mybatis.banner;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.file.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;

class BannerRowMapperTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 3, 1, 0, 0);

    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 4, 1, 0, 0);

    @Test
    @DisplayName("Banner → 쓰기 행 변환 시 모든 컬럼 값과 전달한 감사 시각이 옮겨진다")
    void toWriteRowCopiesColumns() {
        BannerWriteRow row = BannerRowMapper.toWriteRow(fullBanner(), CREATED_AT, UPDATED_AT);

        assertThat(row.getId()).isEqualTo(11L);
        assertThat(row.getType()).isEqualTo("SIDEBAR");
        assertThat(row.getTitle()).isEqualTo("배너 제목");
        assertThat(row.getImageFileId()).isEqualTo(22L);
        assertThat(row.getLinkUrl()).isEqualTo("https://link");
        assertThat(row.getStartDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(row.getEndDate()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));
        assertThat(row.getSort()).isEqualTo(5);
        assertThat(row.isVisible()).isTrue();
        assertThat(row.isDeleted()).isFalse();
        assertThat(row.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(row.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("조회 행 → Banner 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainFromRowRestoresAllFields() {
        BannerRow row = new BannerRow(
            11L, "SIDEBAR", "배너 제목", 22L, "https://link",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            5, true, false, CREATED_AT, UPDATED_AT);

        Banner restored = BannerRowMapper.toDomain(row);

        assertThat(restored).usingRecursiveComparison().isEqualTo(fullBanner());
    }

    @Test
    @DisplayName("쓰기 행 → Banner 변환은 저장 후 돌려줄 도메인을 그대로 복원한다")
    void toDomainFromWriteRowRestoresAllFields() {
        BannerWriteRow row = BannerRowMapper.toWriteRow(fullBanner(), CREATED_AT, UPDATED_AT);

        assertThat(BannerRowMapper.toDomain(row)).usingRecursiveComparison().isEqualTo(fullBanner());
    }

    @Test
    @DisplayName("boolean 필드와 nullable 필드가 뒤바뀌지 않는다")
    void booleanAndNullableFields() {
        Banner original = Banner.reconstitute(
            12L, BannerType.HOME, null, null, null, null, null, 1, false, true, null, null);

        BannerWriteRow row = BannerRowMapper.toWriteRow(original, null, null);

        assertThat(row.getType()).isEqualTo("HOME");
        assertThat(row.getTitle()).isNull();
        assertThat(row.getImageFileId()).isNull();
        assertThat(row.getLinkUrl()).isNull();
        assertThat(row.getStartDate()).isNull();
        assertThat(row.getEndDate()).isNull();
        assertThat(row.isVisible()).isFalse();
        assertThat(row.isDeleted()).isTrue();
        assertThat(BannerRowMapper.toDomain(row)).usingRecursiveComparison().isEqualTo(original);
    }

    private static Banner fullBanner() {
        return Banner.reconstitute(
            11L, BannerType.SIDEBAR, "배너 제목", UploadedFileId.of(22L), "https://link",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            5, true, false, CREATED_AT, UPDATED_AT);
    }
}
