package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;

public interface TagPersistencePort {
    Optional<Tag> findByTagName(String tagName);

    Tag save(Tag tag);

    void deleteById(Long id);
}
