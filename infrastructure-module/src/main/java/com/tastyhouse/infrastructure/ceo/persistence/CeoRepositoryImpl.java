package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;
import com.tastyhouse.core.domain.ceo.domain.repository.CeoRepository;

@Repository
@RequiredArgsConstructor
public class CeoRepositoryImpl implements CeoRepository {

    private final CeoJpaRepository ceoJpaRepository;

    @Override
    public Optional<Ceo> findByUsername(String username) {
        return ceoJpaRepository.findByUsername(username).map(CeoMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return ceoJpaRepository.existsByUsername(username);
    }

    @Override
    public Ceo save(Ceo ceo) {
        CeoJpaEntity saved = ceoJpaRepository.save(CeoMapper.toEntity(ceo));
        return CeoMapper.toDomain(saved);
    }
}
