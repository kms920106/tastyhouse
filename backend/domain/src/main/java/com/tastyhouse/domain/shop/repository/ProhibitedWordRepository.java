package com.tastyhouse.domain.shop.repository;

import java.util.List;

import com.tastyhouse.domain.shop.model.ProhibitedWord;

public interface ProhibitedWordRepository {

    List<ProhibitedWord> findAll();
}
