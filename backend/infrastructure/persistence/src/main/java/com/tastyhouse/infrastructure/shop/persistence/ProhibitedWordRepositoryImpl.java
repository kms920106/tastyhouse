package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.model.ProhibitedWord;

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
