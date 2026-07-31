package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.domain.model.ProhibitedWord;
import com.tastyhouse.domain.shop.domain.repository.ProhibitedWordRepository;

@Repository
@RequiredArgsConstructor
public class ProhibitedWordRepositoryImpl implements ProhibitedWordRepository {

    private final ProhibitedWordJpaRepository jpaRepository;

    @Override
    public List<ProhibitedWord> findAll() {
        return jpaRepository.findAll().stream()
            .map(ProhibitedWordMapper::toDomain)
            .toList();
    }
}
