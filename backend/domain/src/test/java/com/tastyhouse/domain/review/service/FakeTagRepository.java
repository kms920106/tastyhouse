package com.tastyhouse.domain.review.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.domain.shop.repository.TagRepository;

/**
 * 태그 write 포트의 인메모리 fake.
 */
public class FakeTagRepository implements TagRepository {

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
