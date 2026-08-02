package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.repository.StationRepository;

@Repository
public class StationRepositoryImpl implements StationRepository {

    private final StationJpaRepository stationJpaRepository;

    public StationRepositoryImpl(StationJpaRepository stationJpaRepository) {
        this.stationJpaRepository = stationJpaRepository;
    }

    @Override
    public boolean existsById(Long id) {
        return stationJpaRepository.existsById(id);
    }
}
