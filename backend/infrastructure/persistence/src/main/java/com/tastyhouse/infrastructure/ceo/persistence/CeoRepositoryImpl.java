package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;

@Repository
public class CeoRepositoryImpl implements CeoRepository {

    private final CeoJpaRepository ceoJpaRepository;

    public CeoRepositoryImpl(CeoJpaRepository ceoJpaRepository) {
        this.ceoJpaRepository = ceoJpaRepository;
    }

    @Override
    public Optional<Ceo> findById(CeoId id) {
        return ceoJpaRepository.findById(id.value()).map(CeoMapper::toDomain);
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
