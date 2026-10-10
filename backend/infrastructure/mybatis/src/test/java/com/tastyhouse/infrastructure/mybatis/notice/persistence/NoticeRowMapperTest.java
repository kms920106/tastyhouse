package com.tastyhouse.infrastructure.mybatis.notice.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.notice.model.Notice;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeRowMapperTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 3, 1, 0, 0);

    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 4, 1, 0, 0);

    @Test
    @DisplayName("Notice → 쓰기 행 변환 시 모든 컬럼 값과 전달한 감사 시각이 옮겨진다")
    void toWriteRowCopiesColumns() {
        NoticeWriteRow row = NoticeRowMapper.toWriteRow(fullNotice(), CREATED_AT, UPDATED_AT);

        assertThat(row.getId()).isEqualTo(7L);
        assertThat(row.getTitle()).isEqualTo("제목");
        assertThat(row.getContent()).isEqualTo("본문");
        assertThat(row.isVisible()).isTrue();
        assertThat(row.isDeleted()).isFalse();
        assertThat(row.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(row.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    @DisplayName("조회 행 → Notice 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainFromRowRestoresAllFields() {
        NoticeRow row = new NoticeRow(7L, "제목", "본문", true, false, CREATED_AT, UPDATED_AT);

        Notice restored = NoticeRowMapper.toDomain(row);

        assertThat(restored).usingRecursiveComparison().isEqualTo(fullNotice());
    }

    @Test
    @DisplayName("쓰기 행 → Notice 변환은 저장 후 돌려줄 도메인을 그대로 복원한다")
    void toDomainFromWriteRowRestoresAllFields() {
        NoticeWriteRow row = NoticeRowMapper.toWriteRow(fullNotice(), CREATED_AT, UPDATED_AT);

        assertThat(NoticeRowMapper.toDomain(row)).usingRecursiveComparison().isEqualTo(fullNotice());
    }

    @Test
    @DisplayName("boolean 필드가 뒤바뀌지 않는다")
    void booleanFieldsAreNotSwapped() {
        Notice original = Notice.reconstitute(8L, "t", "c", false, true, null, null);

        NoticeWriteRow row = NoticeRowMapper.toWriteRow(original, null, null);

        assertThat(row.isVisible()).isFalse();
        assertThat(row.isDeleted()).isTrue();
        assertThat(NoticeRowMapper.toDomain(row)).usingRecursiveComparison().isEqualTo(original);
    }

    private static Notice fullNotice() {
        return Notice.reconstitute(7L, "제목", "본문", true, false, CREATED_AT, UPDATED_AT);
    }
}
