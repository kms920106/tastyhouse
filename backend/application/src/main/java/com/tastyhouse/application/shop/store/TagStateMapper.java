package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.application.shop.port.out.write.TagState;

final class TagStateMapper {
    private TagStateMapper() {
    }

    static Tag toDomain(TagState state) {
        return Tag.reconstitute(
            state.id(),
            state.tagName()
        );
    }

    static TagState toState(Tag tag) {
        return new TagState(
            tag.getId(),
            tag.getTagName()
        );
    }
}
