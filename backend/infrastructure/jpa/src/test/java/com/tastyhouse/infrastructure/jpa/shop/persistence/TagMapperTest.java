package com.tastyhouse.infrastructure.jpa.shop.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.Tag;

import static org.assertj.core.api.Assertions.assertThat;

class TagMapperTest {

    @Test
    @DisplayName("Tag 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityTag() {
        Tag original = Tag.reconstitute(
            168L,
            "v69"
        );

        TagJpaEntity entity = TagMapper.toEntity(original);

        assertThat(entity.getTagName()).isEqualTo("v69");
    }

    @Test
    @DisplayName("Tag 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainTag() {
        Tag original = Tag.reconstitute(
            168L,
            "v69"
        );

        TagJpaEntity entity = TagJpaEntity.create(
            "v69"
        );
        ReflectionTestUtils.setField(entity, "id", 168L);

        assertThat(TagMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
