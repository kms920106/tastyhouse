package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.ceo.domain.model.Ceo;
import com.tastyhouse.domain.ceo.domain.repository.CeoRepository;

@Repository
public class CeoRepositoryImpl implements CeoRepository {

    private final CeoJpaRepository ceoJpaRepository;

    public CeoRepositoryImpl(CeoJpaRepository ceoJpaRepository) {
        this.ceoJpaRepository = ceoJpaRepository;
    }

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
