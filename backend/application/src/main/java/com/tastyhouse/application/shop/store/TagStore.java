package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.application.shop.port.out.write.TagStatePort;

public class TagStore implements TagRepository {
    private final TagStatePort tagStatePort;

    public TagStore(TagStatePort tagStatePort) {
        this.tagStatePort = tagStatePort;
    }

    @Override
    public Optional<Tag> findByTagName(String tagName) {
        return tagStatePort.findByTagName(tagName).map(TagStateMapper::toDomain);
    }

    @Override
    public Tag save(Tag tag) {
        return TagStateMapper.toDomain(tagStatePort.save(TagStateMapper.toState(tag)));
    }

    @Override
    public void deleteById(Long id) {
        tagStatePort.deleteById(id);
    }
}
