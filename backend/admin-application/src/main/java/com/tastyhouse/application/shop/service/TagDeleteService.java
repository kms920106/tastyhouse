package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.TagDeleteCommand;
import com.tastyhouse.application.shop.port.in.TagDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.TagSavePort;

@Service
@Transactional
class TagDeleteService implements TagDeleteUseCase {

    private final TagSavePort tagSavePort;

    public TagDeleteService(TagSavePort tagSavePort) {
        this.tagSavePort = tagSavePort;
    }

    @Override
    public void deleteTag(TagDeleteCommand command) {
        Long id = command.tagId();

        tagSavePort.deleteById(id);
    }
}
