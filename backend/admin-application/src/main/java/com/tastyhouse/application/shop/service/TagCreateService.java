package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.application.shop.port.in.TagCreateCommand;
import com.tastyhouse.application.shop.port.in.TagCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.TagSavePort;

@Service
@Transactional
class TagCreateService implements TagCreateUseCase {

    private final TagSavePort tagSavePort;

    public TagCreateService(TagSavePort tagSavePort) {
        this.tagSavePort = tagSavePort;
    }

    @Override
    public Long createTag(TagCreateCommand command) {
        String tagName = command.tagName();

        Tag tag = tagSavePort.save(Tag.of(tagName));
        return tag.getId();
    }
}
