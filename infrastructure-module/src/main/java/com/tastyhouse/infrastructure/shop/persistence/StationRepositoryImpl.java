package com.tastyhouse.infrastructure.shop.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.domain.repository.StationRepository;

@Repository
@RequiredArgsConstructor
public class StationRepositoryImpl implements StationRepository {

    private final StationJpaRepository stationJpaRepository;

    @Override
    public boolean existsById(Long id) {
        return stationJpaRepository.existsById(id);
    }
}
