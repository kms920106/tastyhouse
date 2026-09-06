package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;

public interface TagRepository {
    Optional<Tag> findByTagName(String tagName);

    Tag save(Tag tag);

    void deleteById(Long id);
}
