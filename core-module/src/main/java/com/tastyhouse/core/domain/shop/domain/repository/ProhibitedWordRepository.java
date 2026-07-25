package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.shop.domain.model.ProhibitedWord;

public interface ProhibitedWordRepository {

    List<ProhibitedWord> findAll();
}
