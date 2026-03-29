package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {

    List<String> findTagNamesByIds(List<Long> tagIds);

    Optional<Tag> findByTagName(String tagName);

    Tag save(Tag tag);
}
