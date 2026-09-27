package com.tastyhouse.application.shop.store;

import java.util.List;

import com.tastyhouse.domain.shop.model.ProhibitedWord;

public interface ProhibitedWordRepository {

    List<ProhibitedWord> findAll();
}
