package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.ceo.port.out.write.CeoState;
import com.tastyhouse.application.ceo.port.out.write.CeoStatePort;

@Repository
public class CeoStatePortImpl implements CeoStatePort {
    private final CeoJpaRepository ceoJpaRepository;

    public CeoStatePortImpl(CeoJpaRepository ceoJpaRepository) {
        this.ceoJpaRepository = ceoJpaRepository;
    }

    @Override
    public Optional<CeoState> findById(Long id) {
        return ceoJpaRepository.findById(id).map(CeoMapper::toState);
    }

    @Override
    public Optional<CeoState> findByUsername(String username) {
        return ceoJpaRepository.findByUsername(username).map(CeoMapper::toState);
    }

    @Override
    public boolean existsByUsername(String username) {
        return ceoJpaRepository.existsByUsername(username);
    }

    @Override
    public CeoState save(CeoState state) {
        CeoJpaEntity saved = ceoJpaRepository.save(CeoMapper.toEntity(state));
        return CeoMapper.toState(saved);
    }
}
