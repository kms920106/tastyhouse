package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.shop.domain.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {

    List<String> findTagNamesByIds(List<Long> tagIds);

    Optional<Tag> findByTagName(String tagName);

    Tag save(Tag tag);
}
