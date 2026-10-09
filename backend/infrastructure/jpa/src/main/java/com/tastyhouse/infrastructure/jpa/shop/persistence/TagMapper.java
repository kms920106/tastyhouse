package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.Tag;

final class TagMapper {

    private TagMapper() {
    }

    static Tag toDomain(TagJpaEntity entity) {
        return Tag.reconstitute(
            entity.getId(),
            entity.getTagName()
        );
    }

    static TagJpaEntity toEntity(Tag tag) {
        return TagJpaEntity.create(tag.getTagName());
    }
}
