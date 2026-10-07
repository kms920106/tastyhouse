package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.application.shop.port.in.TagCreateCommand;
import com.tastyhouse.application.shop.port.in.TagCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.TagPersistencePort;

@Service
@Transactional
class TagCreateService implements TagCreateUseCase {

    private final TagPersistencePort tagPersistencePort;

    public TagCreateService(TagPersistencePort tagPersistencePort) {
        this.tagPersistencePort = tagPersistencePort;
    }

    @Override
    public Long createTag(TagCreateCommand command) {
        String tagName = command.tagName();

        Tag tag = tagPersistencePort.save(Tag.of(tagName));
        return tag.getId();
    }
}
