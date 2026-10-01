package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordPersistencePort;

@Repository
public class ProhibitedWordPersistenceAdapter implements ProhibitedWordPersistencePort {

    private final ProhibitedWordJpaRepository jpaRepository;

    public ProhibitedWordPersistenceAdapter(ProhibitedWordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ProhibitedWord> findAll() {
        return jpaRepository.findAll().stream()
            .map(ProhibitedWordMapper::toDomain)
            .toList();
    }
}
