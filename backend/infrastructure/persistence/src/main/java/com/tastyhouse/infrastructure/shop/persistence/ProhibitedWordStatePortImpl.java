package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ProhibitedWordState;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordStatePort;

@Repository
public class ProhibitedWordStatePortImpl implements ProhibitedWordStatePort {
    private final ProhibitedWordJpaRepository jpaRepository;

    public ProhibitedWordStatePortImpl(ProhibitedWordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ProhibitedWordState> findAll() {
        return jpaRepository.findAll().stream()
            .map(ProhibitedWordMapper::toState)
            .toList();
    }
}
