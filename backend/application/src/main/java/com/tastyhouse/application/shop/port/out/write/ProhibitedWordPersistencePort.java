package com.tastyhouse.application.shop.port.out.write;

import java.util.List;

import com.tastyhouse.domain.shop.model.ProhibitedWord;

public interface ProhibitedWordPersistencePort {

    List<ProhibitedWord> findAll();
}
