package com.tastyhouse.infrastructure.jpa.banner.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.file.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;

class BannerMapperTest {

    @Test
    @DisplayName("Banner → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        Banner original = fullBanner();

        BannerJpaEntity entity = BannerMapper.toEntity(original);

        assertThat(entity.getType()).isEqualTo("SIDEBAR");
        assertThat(entity.getTitle()).isEqualTo("배너 제목");
        assertThat(entity.getImageFileId()).isEqualTo(22L);
        assertThat(entity.getLinkUrl()).isEqualTo("https://link");
        assertThat(entity.getStartDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(entity.getEndDate()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));
        assertThat(entity.getSort()).isEqualTo(5);
        assertThat(entity.isVisible()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("엔티티 → Banner 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        Banner original = fullBanner();

        Banner restored = BannerMapper.toDomain(persisted(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("boolean 필드와 nullable 필드가 뒤바뀌지 않는다")
    void booleanAndNullableFields() {
        Banner original = Banner.reconstitute(
            12L, BannerType.HOME, null, null, null, null, null, 1, false, true, null, null);

        BannerJpaEntity entity = BannerMapper.toEntity(original);

        assertThat(entity.getType()).isEqualTo("HOME");
        assertThat(entity.getTitle()).isNull();
        assertThat(entity.getImageFileId()).isNull();
        assertThat(entity.getLinkUrl()).isNull();
        assertThat(entity.getStartDate()).isNull();
        assertThat(entity.getEndDate()).isNull();
        assertThat(entity.isVisible()).isFalse();
        assertThat(entity.isDeleted()).isTrue();
        assertThat(BannerMapper.toDomain(persisted(original))).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("applyChanges는 쓰기 가능한 컬럼을 모두 옮긴다")
    void applyChangesCopiesWritableFields() {
        BannerJpaEntity entity = BannerMapper.toEntity(Banner.reconstitute(
            12L, BannerType.HOME, null, null, null, null, null, 1, false, true, null, null));

        BannerMapper.applyChanges(entity, fullBanner());

        assertThat(entity.getType()).isEqualTo("SIDEBAR");
        assertThat(entity.getTitle()).isEqualTo("배너 제목");
        assertThat(entity.getImageFileId()).isEqualTo(22L);
        assertThat(entity.getLinkUrl()).isEqualTo("https://link");
        assertThat(entity.getSort()).isEqualTo(5);
        assertThat(entity.isVisible()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
    }

    private static Banner fullBanner() {
        return Banner.reconstitute(
            11L, BannerType.SIDEBAR, "배너 제목", UploadedFileId.of(22L), "https://link",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            5, true, false,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0));
    }

    private static BannerJpaEntity persisted(Banner banner) {
        BannerJpaEntity entity = BannerMapper.toEntity(banner);
        ReflectionTestUtils.setField(entity, "id", banner.getId());
        ReflectionTestUtils.setField(entity, "createdAt", banner.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", banner.getUpdatedAt());
        return entity;
    }
}
