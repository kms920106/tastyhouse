package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;

public interface TagRepository {
    Optional<Tag> findByTagName(String tagName);

    Tag save(Tag tag);

    void deleteById(Long id);
}
