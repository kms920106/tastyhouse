package com.tastyhouse.infrastructure.jpa.notice.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.notice.model.Notice;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeJpaMapperTest {

    @Test
    @DisplayName("Notice → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        Notice original = Notice.reconstitute(
            7L, "제목", "본문", true, false,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 6, 7, 8, 9, 10));

        NoticeJpaEntity entity = NoticeJpaMapper.toEntity(original);

        assertThat(entity.getTitle()).isEqualTo("제목");
        assertThat(entity.getContent()).isEqualTo("본문");
        assertThat(entity.isVisible()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("엔티티 → Notice 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        Notice original = Notice.reconstitute(
            7L, "제목", "본문", true, false,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 6, 7, 8, 9, 10));

        Notice restored = NoticeJpaMapper.toDomain(persisted(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("boolean 필드가 뒤바뀌지 않는다")
    void booleanFieldsAreNotSwapped() {
        Notice original = Notice.reconstitute(
            8L, "t", "c", false, true,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0));

        NoticeJpaEntity entity = NoticeJpaMapper.toEntity(original);

        assertThat(entity.isVisible()).isFalse();
        assertThat(entity.isDeleted()).isTrue();
        assertThat(NoticeJpaMapper.toDomain(persisted(original))).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("applyChanges는 제목·본문·노출·삭제 여부를 옮긴다")
    void applyChangesCopiesWritableFields() {
        NoticeJpaEntity entity = NoticeJpaMapper.toEntity(Notice.reconstitute(8L, "t", "c", false, true, null, null));

        NoticeJpaMapper.applyChanges(entity, Notice.reconstitute(8L, "제목", "본문", true, false, null, null));

        assertThat(entity.getTitle()).isEqualTo("제목");
        assertThat(entity.getContent()).isEqualTo("본문");
        assertThat(entity.isVisible()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
    }

    private static NoticeJpaEntity persisted(Notice notice) {
        NoticeJpaEntity entity = NoticeJpaMapper.toEntity(notice);
        ReflectionTestUtils.setField(entity, "id", notice.getId());
        ReflectionTestUtils.setField(entity, "createdAt", notice.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", notice.getUpdatedAt());
        return entity;
    }
}
