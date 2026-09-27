package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoOwnerQueryUseCase;
import com.tastyhouse.application.ceo.store.CeoRepository;
import com.tastyhouse.domain.ceo.model.Ceo;

@Service
@CeoApp
@Transactional(readOnly = true)
public class CeoOwnerQueryService implements CeoOwnerQueryUseCase {

    private final CeoRepository ceoRepository;

    public CeoOwnerQueryService(CeoRepository ceoRepository) {
        this.ceoRepository = ceoRepository;
    }

    public Optional<Ceo> findByUsername(String username) {
        return ceoRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return ceoRepository.existsByUsername(username);
    }
}
