package com.tastyhouse.testsupport.review.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.application.shop.port.out.write.TagLoadPort;
import com.tastyhouse.application.shop.port.out.write.TagSavePort;

public class FakeTagPersistence implements TagLoadPort, TagSavePort {

    private final Map<Long, Tag> tags = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Optional<Tag> findByTagName(String tagName) {
        return tags.values().stream().filter(tag -> tag.getTagName().equals(tagName)).findFirst();
    }

    @Override
    public Tag save(Tag tag) {
        Tag persisted = Tag.reconstitute(++sequence, tag.getTagName());
        tags.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public void deleteById(Long id) {
        tags.remove(id);
    }
}
