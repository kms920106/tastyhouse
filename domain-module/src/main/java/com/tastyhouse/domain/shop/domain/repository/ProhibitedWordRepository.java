package com.tastyhouse.domain.shop.domain.repository;

import java.util.List;

import com.tastyhouse.domain.shop.domain.model.ProhibitedWord;

public interface ProhibitedWordRepository {

    List<ProhibitedWord> findAll();
}
