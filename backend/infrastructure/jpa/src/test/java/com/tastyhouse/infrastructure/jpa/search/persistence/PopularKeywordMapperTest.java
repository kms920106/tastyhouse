package com.tastyhouse.infrastructure.jpa.search.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.search.model.PopularKeyword;

import static org.assertj.core.api.Assertions.assertThat;

class PopularKeywordMapperTest {

    @Test
    @DisplayName("PopularKeyword → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        PopularKeyword popularKeyword = PopularKeyword.reconstitute(91L, "치킨", 2, true, false);

        PopularKeywordJpaEntity entity = PopularKeywordMapper.toEntity(popularKeyword);

        assertThat(entity.getKeyword()).isEqualTo("치킨");
        assertThat(entity.getRank()).isEqualTo(2);
        assertThat(entity.isNewKeyword()).isTrue();
        assertThat(entity.isVisible()).isFalse();
    }

    @Test
    @DisplayName("PopularKeyword → 엔티티 변환 시 newKeyword·visible boolean이 뒤바뀌지 않는다")
    void toEntityBooleansAreNotSwapped() {
        PopularKeyword popularKeyword = PopularKeyword.reconstitute(92L, "피자", 5, false, true);

        PopularKeywordJpaEntity entity = PopularKeywordMapper.toEntity(popularKeyword);

        assertThat(entity.getKeyword()).isEqualTo("피자");
        assertThat(entity.getRank()).isEqualTo(5);
        assertThat(entity.isNewKeyword()).isFalse();
        assertThat(entity.isVisible()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → PopularKeyword 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomain() {
        PopularKeywordJpaEntity entity = PopularKeywordJpaEntity.create("치킨", 2, true, false);
        ReflectionTestUtils.setField(entity, "id", 91L);

        PopularKeyword popularKeyword = PopularKeywordMapper.toDomain(entity);

        assertThat(popularKeyword).usingRecursiveComparison()
            .isEqualTo(PopularKeyword.reconstitute(91L, "치킨", 2, true, false));
    }

    @Test
    @DisplayName("엔티티 → PopularKeyword 변환 시 newKeyword·visible boolean이 뒤바뀌지 않는다")
    void toDomainBooleansAreNotSwapped() {
        PopularKeywordJpaEntity entity = PopularKeywordJpaEntity.create("피자", 5, false, true);
        ReflectionTestUtils.setField(entity, "id", 92L);

        PopularKeyword popularKeyword = PopularKeywordMapper.toDomain(entity);

        assertThat(popularKeyword).usingRecursiveComparison()
            .isEqualTo(PopularKeyword.reconstitute(92L, "피자", 5, false, true));
    }
}
