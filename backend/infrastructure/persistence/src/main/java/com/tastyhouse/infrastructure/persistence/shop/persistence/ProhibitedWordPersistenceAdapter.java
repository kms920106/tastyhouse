package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordLoadPort;

@Repository
class ProhibitedWordPersistenceAdapter implements ProhibitedWordLoadPort {

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
