package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.search.model.SearchKeywordLog;

import static org.assertj.core.api.Assertions.assertThat;

class SearchKeywordLogMapperTest {

    @Test
    @DisplayName("SearchKeywordLog → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        SearchKeywordLog log = SearchKeywordLog.reconstitute(93L, "떡볶이", LocalDateTime.of(2026, 9, 1, 12, 30));

        SearchKeywordLogJpaEntity entity = SearchKeywordLogMapper.toEntity(log);

        assertThat(entity.getKeyword()).isEqualTo("떡볶이");
        assertThat(entity.getSearchedAt()).isEqualTo(LocalDateTime.of(2026, 9, 1, 12, 30));
    }

    @Test
    @DisplayName("엔티티 → SearchKeywordLog 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomain() {
        SearchKeywordLogJpaEntity entity = SearchKeywordLogJpaEntity.create("떡볶이", LocalDateTime.of(2026, 9, 1, 12, 30));
        ReflectionTestUtils.setField(entity, "id", 93L);

        SearchKeywordLog log = SearchKeywordLogMapper.toDomain(entity);

        assertThat(log).usingRecursiveComparison()
            .isEqualTo(SearchKeywordLog.reconstitute(93L, "떡볶이", LocalDateTime.of(2026, 9, 1, 12, 30)));
    }
}
