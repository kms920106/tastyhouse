package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.TagDeleteCommand;
import com.tastyhouse.application.shop.port.in.TagDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.TagPersistencePort;

@Service
@Transactional
class TagDeleteService implements TagDeleteUseCase {

    private final TagPersistencePort tagPersistencePort;

    public TagDeleteService(TagPersistencePort tagPersistencePort) {
        this.tagPersistencePort = tagPersistencePort;
    }

    @Override
    public void deleteTag(TagDeleteCommand command) {
        Long id = command.tagId();

        tagPersistencePort.deleteById(id);
    }
}
