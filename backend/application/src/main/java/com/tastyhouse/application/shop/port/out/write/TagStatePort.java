package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface TagStatePort {
    Optional<TagState> findByTagName(String tagName);

    TagState save(TagState tag);

    void deleteById(Long id);
}
