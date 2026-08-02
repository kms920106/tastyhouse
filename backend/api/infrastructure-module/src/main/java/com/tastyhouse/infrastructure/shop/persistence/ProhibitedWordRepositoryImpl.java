package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;

@Repository
public class ProhibitedWordRepositoryImpl implements ProhibitedWordRepository {

    private final ProhibitedWordJpaRepository jpaRepository;

    public ProhibitedWordRepositoryImpl(ProhibitedWordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ProhibitedWord> findAll() {
        return jpaRepository.findAll().stream()
            .map(ProhibitedWordMapper::toDomain)
            .toList();
    }
}
