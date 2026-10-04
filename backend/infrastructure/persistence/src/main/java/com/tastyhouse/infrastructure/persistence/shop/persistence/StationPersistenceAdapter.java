package com.tastyhouse.infrastructure.persistence.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.StationPersistencePort;

@Repository
public class StationPersistenceAdapter implements StationPersistencePort {

    private final StationJpaRepository stationJpaRepository;

    public StationPersistenceAdapter(StationJpaRepository stationJpaRepository) {
        this.stationJpaRepository = stationJpaRepository;
    }

    @Override
    public boolean existsById(Long id) {
        return stationJpaRepository.existsById(id);
    }
}
