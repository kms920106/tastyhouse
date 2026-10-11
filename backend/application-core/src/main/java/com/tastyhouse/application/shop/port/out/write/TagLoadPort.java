package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;

public interface TagLoadPort {

    Optional<Tag> findByTagName(String tagName);
}
