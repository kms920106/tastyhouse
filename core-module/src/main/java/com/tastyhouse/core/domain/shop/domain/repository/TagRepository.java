package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.Tag;

public interface TagRepository {

    List<String> findTagNamesByIds(List<Long> tagIds);

    Optional<Tag> findByTagName(String tagName);

    Tag save(Tag tag);
}
