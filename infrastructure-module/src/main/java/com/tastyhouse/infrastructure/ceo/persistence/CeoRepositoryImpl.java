package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;
import com.tastyhouse.core.domain.ceo.domain.repository.CeoRepository;
import com.tastyhouse.core.domain.ceo.domain.vo.CeoId;

@Repository
@RequiredArgsConstructor
public class CeoRepositoryImpl implements CeoRepository {

    private final CeoJpaRepository ceoJpaRepository;

    @Override
    public Optional<Ceo> findById(CeoId ceoId) {
        return ceoJpaRepository.findById(ceoId.value()).map(CeoMapper::toDomain);
    }

    @Override
    public List<Ceo> findAll() {
        return ceoJpaRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
            .map(CeoMapper::toDomain)
            .toList();
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
