package com.tastyhouse.application.shop.store;

import java.util.List;

import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordStatePort;

public class ProhibitedWordStore implements ProhibitedWordRepository {
    private final ProhibitedWordStatePort prohibitedWordStatePort;

    public ProhibitedWordStore(ProhibitedWordStatePort prohibitedWordStatePort) {
        this.prohibitedWordStatePort = prohibitedWordStatePort;
    }

    @Override
    public List<ProhibitedWord> findAll() {
        return prohibitedWordStatePort.findAll().stream()
            .map(ProhibitedWordStateMapper::toDomain)
            .toList();
    }
}
