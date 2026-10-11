package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.Tag;

public interface TagSavePort {

    Tag save(Tag tag);

    void deleteById(Long id);
}
