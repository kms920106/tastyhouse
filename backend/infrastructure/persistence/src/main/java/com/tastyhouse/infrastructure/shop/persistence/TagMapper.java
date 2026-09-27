package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.TagState;

final class TagMapper {
    private TagMapper() {
    }

    static TagState toState(TagJpaEntity entity) {
        return new TagState(
            entity.getId(),
            entity.getTagName()
        );
    }

    static TagJpaEntity toEntity(TagState state) {
        return TagJpaEntity.create(state.tagName());
    }
}
